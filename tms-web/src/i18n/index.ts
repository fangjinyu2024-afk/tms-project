import { createI18n } from 'vue-i18n'
import elementZhCn from 'element-plus/es/locale/lang/zh-cn'
import elementEn from 'element-plus/es/locale/lang/en'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

export type AppLocale = 'zh-CN' | 'en-US'

const STORAGE_KEY = 'tms-locale'

export const LOCALE_OPTIONS: Array<{ value: AppLocale; labelKey: string }> = [
  { value: 'zh-CN', labelKey: 'language.zh' },
  { value: 'en-US', labelKey: 'language.en' }
]

/** 后端按 Accept-Language 返回错误提示与枚举名称，取值与前端语言保持一致 */
export const ACCEPT_LANGUAGE: Record<AppLocale, string> = {
  'zh-CN': 'zh-CN',
  'en-US': 'en'
}

export const ELEMENT_LOCALES = {
  'zh-CN': elementZhCn,
  'en-US': elementEn
}

function detectLocale(): AppLocale {
  const stored = localStorage.getItem(STORAGE_KEY) as AppLocale | null
  if (stored && stored in ELEMENT_LOCALES) {
    return stored
  }
  return navigator.language.toLowerCase().startsWith('zh') ? 'zh-CN' : 'en-US'
}

const i18n = createI18n({
  legacy: false,
  locale: detectLocale(),
  fallbackLocale: 'zh-CN',
  messages: { 'zh-CN': zhCN, 'en-US': enUS }
})

export function currentLocale(): AppLocale {
  return i18n.global.locale.value as AppLocale
}

export function setLocale(locale: AppLocale) {
  i18n.global.locale.value = locale
  localStorage.setItem(STORAGE_KEY, locale)
  document.documentElement.setAttribute('lang', locale)
}

export function translate(key: string, named?: Record<string, unknown>): string {
  return named ? i18n.global.t(key, named) : i18n.global.t(key)
}

document.documentElement.setAttribute('lang', currentLocale())

export default i18n
