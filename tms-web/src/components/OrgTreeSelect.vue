<template>
  <el-tree-select
    class="org-tree-select"
    :model-value="modelValue"
    :data="options"
    :props="treeProps"
    node-key="id"
    check-strictly
    :render-after-expand="false"
    default-expand-all
    clearable
    :placeholder="placeholder ?? t('member.orgPlaceholder')"
    @update:model-value="(value: Id | undefined) => emit('update:modelValue', value)"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { fetchOrgSelector, fetchOrgTree } from '@/api/org'
import type { Id, OrgTreeNode } from '@/api/types'

const props = withDefaults(
  defineProps<{ modelValue?: Id; placeholder?: string; onlyEnabled?: boolean }>(),
  { onlyEnabled: false }
)
const emit = defineEmits<{ (event: 'update:modelValue', value: Id | undefined): void }>()

const { t } = useI18n()
const options = ref<OrgTreeNode[]>([])
const treeProps = { label: 'name', children: 'children' }

onMounted(async () => {
  options.value = props.onlyEnabled ? await fetchOrgSelector() : await fetchOrgTree()
})
</script>

<style scoped>
.org-tree-select {
  width: 220px;
}
</style>
