<template>
  <div>
    <div class="table-top">
      <span class="sub"><slot name="summary">{{ t('common.records', { total }) }}</slot></span>
      <div class="actions"><slot name="actions" /></div>
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th v-for="column in columns" :key="column.key">{{ t(column.labelKey) }}</th>
            <th v-if="$slots.rowActions">{{ t('common.action') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in rows" :key="rowKey(row, index)">
            <td
              v-for="(column, columnIndex) in columns"
              :key="column.key"
              :class="{ maincell: columnIndex === 0 }"
            >
              <slot :name="`cell-${column.key}`" :row="row">{{ row[column.key] ?? t('common.dash') }}</slot>
            </td>
            <td v-if="$slots.rowActions" class="row-actions">
              <slot name="rowActions" :row="row" />
            </td>
          </tr>
        </tbody>
      </table>
      <AppEmpty v-if="!rows.length" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AppEmpty from './AppEmpty.vue'
import type { ContractColumn } from '@/contracts/pages'

/** 列定义必须来自 src/contracts/pages.ts 的契约，不要在页面里手写 */
const props = withDefaults(
  defineProps<{
    columns: ContractColumn[]
    rows: Record<string, any>[]
    total: number
    idField?: string
  }>(),
  { idField: 'id' }
)

const { t } = useI18n()

function rowKey(row: Record<string, any>, index: number) {
  return row[props.idField] ?? index
}
</script>
