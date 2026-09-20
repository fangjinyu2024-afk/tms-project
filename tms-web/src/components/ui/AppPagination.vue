<template>
  <div class="pagination">
    <span>{{ t('common.paginationSummary', { total, size: pageSize }) }}</span>
    <div class="page-buttons">
      <button
        type="button"
        class="page-num"
        :disabled="current <= 1"
        :aria-label="t('common.prevPage')"
        @click="emit('change', current - 1)"
      >
        ‹
      </button>
      <button
        v-for="page in pages"
        :key="page"
        type="button"
        class="page-num"
        :class="{ active: page === current }"
        @click="emit('change', page)"
      >
        {{ page }}
      </button>
      <button
        type="button"
        class="page-num"
        :disabled="current >= pages.length"
        :aria-label="t('common.nextPage')"
        @click="emit('change', current + 1)"
      >
        ›
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{ total: number; current: number; pageSize: number }>()
const emit = defineEmits<{ (event: 'change', page: number): void }>()

const { t } = useI18n()

const pages = computed(() => {
  const count = Math.max(1, Math.ceil(props.total / props.pageSize))
  return Array.from({ length: count }, (_, i) => i + 1)
})
</script>
