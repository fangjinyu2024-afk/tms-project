/**
 * 前端一致性校验。用法：npm run check
 *
 * 多人／多 AI 并行复刻页面时，光靠文档约束会各写各的。这里把规范
 * docs/TMS-前端复刻规范.md 里能机器判定的部分变成会失败的检查。
 *
 * 复刻期内 scripts/check.config.json 的 legacy 列表豁免严格规则，
 * 每迁完一个页面就从 legacy 里删掉它。
 */
import { readFileSync, readdirSync, statSync, existsSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve, relative, join } from 'node:path'

const here = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(here, '..')
const SRC = resolve(ROOT, 'src')
const config = JSON.parse(readFileSync(resolve(here, 'check.config.json'), 'utf8'))
const legacy = new Set(config.legacy)

const failures = []
const fail = (check, detail) => failures.push({ check, detail })

function walk(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    if (statSync(full).isDirectory()) walk(full, out)
    else out.push(full)
  }
  return out
}

const files = walk(SRC)
const rel = (f) => relative(ROOT, f).split('\\').join('/')
const vueFiles = files.filter((f) => f.endsWith('.vue'))
const codeFiles = files.filter((f) => f.endsWith('.vue') || f.endsWith('.ts'))

/** 语言包是纯数据字面量，直接求值，免得为了读两个文件引入 TS 运行时 */
function loadLocale(name) {
  const path = resolve(SRC, `i18n/locales/${name}.ts`)
  const body = readFileSync(path, 'utf8').replace(/^\s*export\s+default\s*/, '')
  // eslint-disable-next-line no-new-func
  return new Function(`return ${body}`)()
}

function flatten(obj, prefix = '', out = new Set()) {
  for (const [k, v] of Object.entries(obj)) {
    const key = prefix ? `${prefix}.${k}` : k
    if (v && typeof v === 'object') flatten(v, key, out)
    else out.add(key)
  }
  return out
}

const zh = flatten(loadLocale('zh-CN'))
const en = flatten(loadLocale('en-US'))

// 1. 中英文键一一对应
{
  const onlyZh = [...zh].filter((k) => !en.has(k))
  const onlyEn = [...en].filter((k) => !zh.has(k))
  if (onlyZh.length) fail('i18n 键对齐', `仅中文有：${onlyZh.join(', ')}`)
  if (onlyEn.length) fail('i18n 键对齐', `仅英文有：${onlyEn.join(', ')}`)
}

// 2. 代码里用到的字面量键必须存在
{
  const missing = new Set()
  for (const f of codeFiles) {
    if (f.includes('/i18n/locales/')) continue
    const text = readFileSync(f, 'utf8')
    for (const m of text.matchAll(/\bt\(\s*'([^']+)'/g)) {
      if (!zh.has(m[1]) || !en.has(m[1])) missing.add(`${rel(f)} → ${m[1]}`)
    }
  }
  if (missing.size) fail('i18n 键存在', [...missing].join('\n      '))
}

/** 去掉注释，避免把中文注释误判成硬编码文案 */
function stripComments(text) {
  return text
    .replace(/<!--[\s\S]*?-->/g, '')
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/(^|[^:])\/\/[^\n]*/g, '$1')
}

// 3. .vue 里禁止硬编码色值：颜色只能来自 src/styles 的令牌
{
  for (const f of vueFiles) {
    if (legacy.has(rel(f))) continue
    const text = readFileSync(f, 'utf8')
    const styles = [...text.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/g)].map((m) => m[1]).join('\n')
    const inline = [...text.matchAll(/style="([^"]*)"/g)].map((m) => m[1]).join('\n')
    const hits = [...`${styles}\n${inline}`.matchAll(/#[0-9a-fA-F]{3,8}\b/g)].map((m) => m[0])
    if (hits.length) {
      fail('硬编码色值', `${rel(f)} 出现 ${[...new Set(hits)].join(', ')}，改用 src/styles/tokens.css 的变量`)
    }
  }
}

// 4. .vue 里禁止硬编码中日韩文案，必须走 i18n
{
  for (const f of vueFiles) {
    if (legacy.has(rel(f))) continue
    const text = stripComments(readFileSync(f, 'utf8'))
    const hits = [...text.matchAll(/[\u4e00-\u9fff\u3040-\u30ff\uac00-\ud7af]+/g)].map((m) => m[0])
    if (hits.length) {
      fail('硬编码文案', `${rel(f)} 出现「${[...new Set(hits)].slice(0, 5).join('、')}」，文案一律进 i18n`)
    }
  }
}

// 5. 页面必须从契约取列与筛选项，且契约文案键中英文齐备
{
  const contractSrc = resolve(SRC, 'contracts/pages.ts')
  if (!existsSync(contractSrc)) {
    fail('页面契约', '缺少 src/contracts/pages.ts，先跑 npm run contracts')
  } else {
    const contractText = readFileSync(contractSrc, 'utf8')
    const keysOf = (menuKey) => {
      const start = contractText.indexOf(`'${menuKey}': {`)
      if (start < 0) return null
      const end = contractText.indexOf('\n  },', start)
      const body = contractText.slice(start, end)
      return [...body.matchAll(/(?:labelKey|descriptionKey|createLabelKey): '([^']+)'/g)].map((m) => m[1])
    }
    for (const [file, menuKey] of Object.entries(config.pages ?? {})) {
      if (legacy.has(file)) continue
      const full = resolve(ROOT, file)
      if (!existsSync(full)) {
        fail('页面契约', `${file} 在 check.config.json 登记了却不存在`)
        continue
      }
      const text = readFileSync(full, 'utf8')
      if (!/from '@\/contracts\/pages'/.test(text) || !text.includes(menuKey)) {
        fail('页面契约', `${file} 没有从 @/contracts/pages 取 '${menuKey}' 的契约，列与筛选项不得手写`)
      }
      const keys = keysOf(menuKey)
      if (!keys) {
        fail('页面契约', `契约里没有 '${menuKey}'`)
        continue
      }
      const lack = keys.filter((k) => !zh.has(k) || !en.has(k))
      if (lack.length) fail('页面契约', `${file} 契约文案键缺中文或英文：${lack.join(', ')}`)
    }
  }
}

// 6. 复刻完的文件不得再引用 element-plus
{
  for (const f of codeFiles) {
    if (legacy.has(rel(f))) continue
    const text = readFileSync(f, 'utf8')
    if (/from '(element-plus|@element-plus\/icons-vue)'/.test(text)) {
      fail('组件库', `${rel(f)} 仍在引用 element-plus，UI 组件用 src/components/ui/ 下的自建组件`)
    }
  }
}

const checks = ['i18n 键对齐', 'i18n 键存在', '硬编码色值', '硬编码文案', '页面契约', '组件库']
for (const name of checks) {
  const hit = failures.filter((f) => f.check === name)
  if (!hit.length) console.log(`  ✓ ${name}`)
  else {
    console.log(`  ✗ ${name}`)
    for (const h of hit) console.log(`      ${h.detail}`)
  }
}

console.log(`\n豁免中的历史文件 ${legacy.size} 个，复刻完成后逐个从 scripts/check.config.json 的 legacy 删除。`)

if (failures.length) {
  console.error(`\n校验未通过：${failures.length} 项。规则见 docs/TMS-前端复刻规范.md`)
  process.exit(1)
}
console.log('\n校验通过')
