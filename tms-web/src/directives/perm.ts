import type { Directive } from 'vue'
import { useUserStore } from '@/stores/user'

/** v-perm="'members:create'" 无权限时移除元素；仅用于界面收敛，服务端仍逐请求校验 */
export const perm: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    const codes = Array.isArray(binding.value) ? binding.value : [binding.value]
    const store = useUserStore()
    if (!codes.some((code) => store.has(code))) {
      el.parentNode?.removeChild(el)
    }
  }
}
