<template>
  <div class="metrics" :class="{ 'metrics-three': items.length === 3 }">
    <div v-for="item in items" :key="item.label" class="metric">
      <div class="metric-label">{{ item.label }}</div>
      <div class="metric-value">
        {{ item.value }}<span>{{ item.unit ?? '' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
export interface Metric {
  label: string
  value: string | number
  unit?: string
}

defineProps<{ items: Metric[] }>()
</script>
