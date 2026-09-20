/**
 * 从 prototype/TMS-原型.html 的 configs{} 抽取页面契约，生成 src/contracts/pages.ts。
 *
 * 用法：npm run contracts
 * 原型改动后重跑本脚本，不要手工改 pages.ts。
 */
import { readFileSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const here = dirname(fileURLToPath(import.meta.url))
const PROTOTYPE = resolve(here, '../../prototype/TMS-原型.html')
const OUTPUT = resolve(here, '../src/contracts/pages.ts')

const html = readFileSync(PROTOTYPE, 'utf8')
const js = [...html.matchAll(/<script[^>]*>([\s\S]*?)<\/script>/g)].map((m) => m[1]).join('')

/** 从 marker 之后做花括号配对，取出完整字面量块 */
function block(marker) {
  const start = js.indexOf(marker)
  if (start < 0) throw new Error(`原型里找不到 ${marker}`)
  let depth = 0
  let buf = ''
  for (const ch of js.slice(start + marker.length)) {
    buf += ch
    if (ch === '{') depth++
    else if (ch === '}' && --depth === 0) return buf
  }
  throw new Error(`${marker} 括号不闭合`)
}

/** 按顶层逗号切分对象字面量的条目 */
function entries(raw) {
  const out = []
  let depth = 0
  let buf = ''
  for (const ch of raw.slice(1, -1)) {
    if (ch === '{' || ch === '[') depth++
    else if (ch === '}' || ch === ']') depth--
    if (ch === ',' && depth === 0) {
      out.push(buf)
      buf = ''
    } else buf += ch
  }
  if (buf.trim()) out.push(buf)
  return out
}

const configsRaw = block('configs=')
const filterEnumsRaw = block('FILTER_ENUMS=')
const descriptionsRaw = block('descriptions=')

const filterEnums = {}
for (const m of filterEnumsRaw.matchAll(/'([\w-]+):(\w+)'\s*:\s*\[([^\]]*)\]/g)) {
  filterEnums[m[1]] ??= {}
  filterEnums[m[1]][m[2]] = [...m[3].matchAll(/'([^']*)'/g)].map((x) => x[1])
}

const descriptions = {}
for (const m of descriptionsRaw.matchAll(/([\w-]+)\s*:\s*'([^']*)'/g)) descriptions[m[1]] = m[2]

const pages = {}
for (const entry of entries(configsRaw)) {
  const head = entry.match(/^\s*(\w[\w-]*)\s*:\s*\{([\s\S]*)\}\s*$/)
  if (!head) continue
  const [, key, body] = head
  const colsAt = body.search(/cols:\s*\[/)
  if (colsAt < 0) continue
  let depth = 0
  let colsRaw = ''
  for (const ch of body.slice(body.indexOf('[', colsAt))) {
    colsRaw += ch
    if (ch === '[') depth++
    else if (ch === ']' && --depth === 0) break
  }
  const grab = (name) => body.match(new RegExp(`${name}:'([^']*)'`))?.[1]
  const filters = []
  if (grab('filterLabel')) filters.push({ field: grab('filter'), label: grab('filterLabel') })
  if (grab('filter2Label')) filters.push({ field: grab('filter2'), label: grab('filter2Label') })
  for (const f of filters) {
    const options = filterEnums[key]?.[f.field]
    if (options) f.options = options
  }
  pages[key] = {
    columns: [...colsRaw.matchAll(/\['([^']+)','([^']+)'\]/g)].map((m) => ({ key: m[1], label: m[2] })),
    filters,
    newLabel: grab('newLabel'),
    description: descriptions[key] ?? '',
  }
}

const camel = (k) => k.split('-').map((p, i) => (i ? p[0].toUpperCase() + p.slice(1) : p)).join('')
const q = (s) => `'${String(s).replace(/'/g, "\\'")}'`

const out = []
out.push(`/**
 * 页面契约：列、列序、筛选项、按钮与说明文字的唯一来源。
 *
 * 由 scripts/extract-contracts.mjs 从 prototype/TMS-原型.html 的 configs{} 抽取，**不要手工编辑**。
 * 复刻页面时从这里取，不要各自去读原型再转述——那是多人协作时页面跑偏的主要原因。
 *
 * prototypeLabel 是原型里的中文原文，仅用于比对核验；页面渲染一律用 labelKey 走 i18n。
 * 筛选项的取值以 stores/dict.ts 的枚举为准，prototypeOptions 只说明原型提供了哪几项。
 */

export interface ContractColumn {
  key: string
  labelKey: string
  prototypeLabel: string
}

export interface ContractFilter {
  field: string
  labelKey: string
  prototypeLabel: string
  prototypeOptions?: string[]
}

export interface PageContract {
  menuKey: string
  descriptionKey: string
  prototypeDescription: string
  createLabelKey?: string
  prototypeCreateLabel?: string
  columns: ContractColumn[]
  filters: ContractFilter[]
}

export const PAGE_CONTRACTS: Record<string, PageContract> = {`)

for (const key of Object.keys(pages).sort()) {
  const p = pages[key]
  const ns = `page.${camel(key)}`
  out.push(`  ${q(key)}: {`)
  out.push(`    menuKey: ${q(key)},`)
  out.push(`    descriptionKey: ${q(`${ns}.description`)},`)
  out.push(`    prototypeDescription: ${q(p.description)},`)
  if (p.newLabel) {
    out.push(`    createLabelKey: ${q(`${ns}.create`)},`)
    out.push(`    prototypeCreateLabel: ${q(p.newLabel)},`)
  }
  out.push('    columns: [')
  for (const c of p.columns) {
    out.push(`      { key: ${q(c.key)}, labelKey: ${q(`${ns}.col.${c.key}`)}, prototypeLabel: ${q(c.label)} },`)
  }
  out.push('    ],')
  out.push('    filters: [')
  for (const f of p.filters) {
    const opts = f.options ? `, prototypeOptions: [${f.options.map(q).join(', ')}]` : ''
    out.push(`      { field: ${q(f.field)}, labelKey: ${q(`${ns}.filter.${f.field}`)}, prototypeLabel: ${q(f.label)}${opts} },`)
  }
  out.push('    ],')
  out.push('  },')
}
out.push('}\n')
out.push(`/** 自定义渲染的页面，原型里没有 configs 条目，契约随各自页面复刻时补入这里。 */
export const BESPOKE_PAGES = [
  'home', 'devices', 'mode', 'ota', 'ota-records', 'rki', 'rki-records', 'sessions',
] as const
`)

writeFileSync(OUTPUT, out.join('\n'), 'utf8')
const cols = Object.values(pages).reduce((n, p) => n + p.columns.length, 0)
console.log(`已生成 ${OUTPUT}`)
console.log(`页面 ${Object.keys(pages).length} 个，列 ${cols} 条`)
