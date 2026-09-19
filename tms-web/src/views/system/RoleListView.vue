<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input
          v-model="query.keyword"
          :placeholder="t('role.namePlaceholder')"
          clearable
          style="width: 200px"
        />
        <el-select
          v-model="query.status"
          :placeholder="t('common.status')"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in ENABLE_STATUS"
            :key="item.value"
            :label="t(item.labelKey)"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" @click="load(1)">{{ t('common.search') }}</el-button>
        <el-button @click="reset">{{ t('common.reset') }}</el-button>
        <span class="grow" />
        <el-button v-perm="'roles:create'" type="primary" @click="openCreate">
          {{ t('role.createTitle') }}
        </el-button>
        <el-button v-perm="'roles:export'" @click="onExport">{{ t('common.export') }}</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column :label="t('role.name')" min-width="180">
          <template #default="{ row }">
            {{ row.name }}
            <el-tag v-if="row.builtin" size="small" type="warning" class="tag">
              {{ t('role.builtin') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerOrgName" :label="t('role.ownerOrg')" min-width="140" />
        <el-table-column prop="dataScopeLabel" :label="t('role.dataScope')" min-width="180" />
        <el-table-column prop="memberCount" :label="t('role.memberCount')" width="90" />
        <el-table-column :label="t('common.status')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ t(`enums.enableStatus.${row.status}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('role.description')" min-width="160" />
        <el-table-column :label="t('common.action')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openView(row)">
              {{ t('role.viewPermissions') }}
            </el-button>
            <el-button
              v-perm="'roles:edit'"
              link
              type="primary"
              :disabled="row.builtin"
              @click="openEdit(row)"
            >
              {{ t('common.edit') }}
            </el-button>
            <el-button v-perm="'roles:copy'" link type="primary" @click="onCopy(row)">
              {{ t('common.copy') }}
            </el-button>
            <el-button
              v-perm="'roles:toggle'"
              link
              type="primary"
              :disabled="row.builtin"
              @click="onToggle(row)"
            >
              {{ row.status === 'ENABLED' ? t('common.disable') : t('common.enable') }}
            </el-button>
            <el-button
              v-perm="'roles:delete'"
              link
              type="danger"
              :disabled="row.builtin"
              @click="onDelete(row)"
            >
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        layout="total, sizes, prev, pager, next"
        :total="page.total"
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('role.name')" prop="name">
              <el-input v-model="form.name" :disabled="readonly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('role.dataScope')" prop="dataScope">
              <el-select v-model="form.dataScope" :disabled="readonly" style="width: 100%">
                <el-option
                  v-for="item in DATA_SCOPE"
                  :key="item.value"
                  :label="t(item.labelKey)"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="!editing" :span="12">
            <el-form-item :label="t('role.ownerOrg')" prop="ownerOrgId">
              <OrgTreeSelect v-model="form.ownerOrgId" only-enabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('role.description')">
              <el-input v-model="form.description" :disabled="readonly" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <p class="form-tip">{{ t('role.matrixTip') }}</p>
      <PermissionMatrix
        :catalog="catalog"
        :selected="form.permCodes"
        :disabled="readonly"
        @update:selected="(value) => (form.permCodes = value)"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">
          {{ readonly ? t('common.close') : t('common.cancel') }}
        </el-button>
        <el-button v-if="!readonly" type="primary" :loading="saving" @click="submit">
          {{ t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import OrgTreeSelect from '@/components/OrgTreeSelect.vue'
import PermissionMatrix from '@/components/PermissionMatrix.vue'
import { DATA_SCOPE, ENABLE_STATUS } from '@/stores/dict'
import { fetchCatalog } from '@/api/permission'
import {
  copyRole,
  createRole,
  deleteRole,
  exportRoles,
  pageRoles,
  roleDetail,
  toggleRole,
  updateRole,
  type RoleQuery
} from '@/api/role'
import type { DataScope, Id, PermissionCatalogItem, RoleItem } from '@/api/types'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const user = useUserStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const readonly = ref(false)
const editing = ref<RoleItem | null>(null)
const formRef = ref<FormInstance>()
const catalog = ref<PermissionCatalogItem[]>([])
const page = reactive({ total: 0, list: [] as RoleItem[] })

const query = reactive<RoleQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: undefined
})

const form = reactive({
  name: '',
  description: '',
  ownerOrgId: undefined as Id | undefined,
  dataScope: 'ORG_AND_SUB' as DataScope,
  permCodes: [] as string[],
  version: 0
})

const rules = computed<FormRules>(() => ({
  name: [{ required: true, message: t('role.nameRequired'), trigger: 'blur' }],
  dataScope: [{ required: true, message: t('role.dataScopeRequired'), trigger: 'change' }],
  ownerOrgId: [{ required: true, message: t('role.ownerOrgRequired'), trigger: 'change' }]
}))

const dialogTitle = computed(() =>
  readonly.value
    ? t('role.permissionTitle')
    : editing.value
      ? t('role.editTitle')
      : t('role.createTitle')
)

watch(
  () => form.ownerOrgId,
  async (orgId) => {
    if (orgId) {
      catalog.value = await fetchCatalog(orgId)
    }
  }
)

onMounted(() => load(1))

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  loading.value = true
  try {
    const result = await pageRoles(query)
    page.total = result.total
    page.list = result.list
  } finally {
    loading.value = false
  }
}

function onSizeChange(size: number) {
  query.pageSize = size
  void load(1)
}

function reset() {
  query.keyword = ''
  query.status = undefined
  void load(1)
}

async function openCreate() {
  editing.value = null
  readonly.value = false
  Object.assign(form, {
    name: '',
    description: '',
    ownerOrgId: user.member?.orgId,
    dataScope: 'ORG_AND_SUB',
    permCodes: [],
    version: 0
  })
  catalog.value = await fetchCatalog(user.member?.orgId)
  dialogVisible.value = true
}

async function openEdit(row: RoleItem) {
  const detail = await roleDetail(row.id)
  editing.value = detail
  readonly.value = false
  Object.assign(form, {
    name: detail.name,
    description: detail.description ?? '',
    ownerOrgId: detail.ownerOrgId,
    dataScope: detail.dataScope,
    permCodes: detail.permCodes ?? [],
    version: detail.version
  })
  catalog.value = await fetchCatalog(detail.ownerOrgId)
  dialogVisible.value = true
}

async function openView(row: RoleItem) {
  await openEdit(row)
  readonly.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateRole(editing.value.id, {
        name: form.name,
        description: form.description,
        dataScope: form.dataScope,
        permCodes: form.permCodes,
        version: form.version
      })
    } else {
      await createRole({
        name: form.name,
        description: form.description,
        ownerOrgId: form.ownerOrgId,
        dataScope: form.dataScope,
        permCodes: form.permCodes
      })
    }
    ElMessage.success(t('common.saved'))
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onCopy(row: RoleItem) {
  await ElMessageBox.confirm(t('role.copyConfirm'), t('role.copyTitle'))
  await copyRole(row.id)
  ElMessage.success(t('role.copied'))
  await load()
}

async function onToggle(row: RoleItem) {
  await toggleRole(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success(t('common.statusUpdated'))
  await load()
}

async function onDelete(row: RoleItem) {
  await ElMessageBox.confirm(t('role.deleteConfirm', { name: row.name }), t('role.deleteTitle'), {
    type: 'warning'
  })
  await deleteRole(row.id)
  ElMessage.success(t('common.deleted'))
  await load()
}

async function onExport() {
  const url = await exportRoles(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.tag {
  margin-left: 6px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
