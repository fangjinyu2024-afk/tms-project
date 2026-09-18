/** 接口时间为 UTC ISO-8601，展示时按浏览器时区转换（详细设计 7.9） */
export function formatDateTime(value?: string | null): string {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/** 本地时间转 UTC ISO 串，用于时间范围筛选 */
export function toUtcIso(value?: Date | string | null): string | undefined {
  if (!value) {
    return undefined
  }
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) {
    return undefined
  }
  return date.toISOString().replace(/\.(\d{3})Z$/, '.$1Z')
}
