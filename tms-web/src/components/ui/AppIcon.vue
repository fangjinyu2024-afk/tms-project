<template>
  <svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.6"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path :d="path" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

/** 图标路径移植自 prototype/TMS-原型.html 的 icons{}，勿手工改动路径数据。 */
export type IconName =
  | 'device'
  | 'org'
  | 'users'
  | 'box'
  | 'task'
  | 'key'
  | 'cert'
  | 'log'
  | 'upload'
  | 'gear'
  | 'plus'
  | 'search'
  | 'shield'


const PATHS: Record<IconName, string> = {
  device: 'M7 2h10v20H7z M10 18h4 M9 5h6v9H9z',
  org: 'M9 2h6v5H9z M3 17h6v5H3z M15 17h6v5h-6z M12 7v5 M6 17v-5h12v5',
  users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 3a4 4 0 1 0 0 8a4 4 0 0 0 0-8 M17 4a4 4 0 0 1 0 8 M22 21v-2a4 4 0 0 0-3-3.9',
  box: 'M3 7l9-5 9 5v10l-9 5-9-5z M3 7l9 5 9-5 M12 12v10',
  task: 'M8 3h8v4H8z M8 5H4v17h16V5h-4 M8 12h8 M8 17h5',
  key: 'M8 3a5 5 0 1 0 0 10a5 5 0 0 0 0-10 M12 11l9 9 M17 16l3-3 M19 18l3-3',
  cert: 'M5 2h10l4 4v11H5z M14 2v5h5 M8 10h7 M8 13h5 M10 17v5l3-2 3 2v-5',
  log: 'M5 3h14v18H5z M8 7h8 M8 11h8 M8 15h5',
  upload: 'M12 16V3 M7 8l5-5 5 5 M4 16v5h16v-5',
  gear: 'M12 8a4 4 0 1 0 0 8a4 4 0 0 0 0-8 M12 2v3 M12 19v3 M2 12h3 M19 12h3 M5 5l2 2 M17 17l2 2 M5 19l2-2 M17 7l2-2',
  plus: 'M12 5v14 M5 12h14',
  search: 'M10 3a7 7 0 1 0 0 14a7 7 0 0 0 0-14 M15 15l6 6',
  shield: 'M12 2l9 4v6c0 6-9 10-9 10S3 18 3 12V6z M8 12l3 3 5-6',
}

const props = withDefaults(defineProps<{ name?: IconName }>(), { name: 'task' })

const path = computed(() => PATHS[props.name] ?? PATHS.task)
</script>

<style scoped>
svg {
  width: 17px;
  height: 17px;
  flex: none;
}
</style>
