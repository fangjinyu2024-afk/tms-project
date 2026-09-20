import { ref } from 'vue'

/**
 * 轻量提示，替代 ElMessage。样式取自原型 .toast，显示时长与原型一致。
 */
export interface ToastState {
  text: string
  tone: 'success' | 'danger'
  visible: boolean
}

const DURATION = 3200

const state = ref<ToastState>({ text: '', tone: 'success', visible: false })
let timer: ReturnType<typeof setTimeout> | undefined

function show(text: string, tone: ToastState['tone']) {
  state.value = { text, tone, visible: true }
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    state.value = { ...state.value, visible: false }
  }, DURATION)
}

export function useToast() {
  return {
    state,
    success: (text: string) => show(text, 'success'),
    error: (text: string) => show(text, 'danger'),
  }
}
