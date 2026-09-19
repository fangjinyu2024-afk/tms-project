<template>
  <el-table :data="catalog" border size="small" class="matrix">
    <el-table-column prop="groupName" :label="t('role.matrixGroup')" width="110" />
    <el-table-column prop="menuName" :label="t('role.matrixMenu')" width="140" />
    <el-table-column :label="t('role.matrixActions')">
      <template #default="{ row }">
        <el-checkbox
          v-for="action in row.actions"
          :key="action.permCode"
          :model-value="selected.includes(action.permCode)"
          :disabled="disabled || !action.grantable"
          class="action"
          @change="(checked: boolean) => toggle(row, action.permCode, checked)"
        >
          {{ action.actionName }}
        </el-checkbox>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { PermissionCatalogItem } from '@/api/types'

const props = defineProps<{
  catalog: PermissionCatalogItem[]
  selected: string[]
  disabled?: boolean
}>()
const emit = defineEmits<{ (event: 'update:selected', value: string[]): void }>()

const { t } = useI18n()

/**
 * 勾选任一操作自动补齐该菜单的查看权限；取消查看时同步取消该菜单下其他操作。
 * 服务端保存时会再次校验依赖（详细设计 3.2.5 第 7 条）。
 */
function toggle(row: PermissionCatalogItem, permCode: string, checked: boolean) {
  const next = new Set(props.selected)
  const viewCode = `${row.menuKey}:view`
  const isView = permCode === viewCode
  if (checked) {
    next.add(permCode)
    if (!isView && row.actions.some((action) => action.permCode === viewCode)) {
      next.add(viewCode)
    }
  } else {
    next.delete(permCode)
    if (isView) {
      row.actions.forEach((action) => {
        if (action.grantable) {
          next.delete(action.permCode)
        }
      })
    }
  }
  emit('update:selected', Array.from(next))
}
</script>

<style scoped>
.matrix {
  max-height: 460px;
  overflow: auto;
}

.action {
  margin-right: 16px;
}
</style>
