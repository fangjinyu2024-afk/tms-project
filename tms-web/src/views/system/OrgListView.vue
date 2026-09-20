<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card>
          <template #header>
            <div>{{ t('org.directory') }}</div>
            <div class="form-tip">{{ t('org.directoryTip') }}</div>
          </template>
          <el-tree
            ref="treeRef"
            :data="tree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="onNodeClick"
          >
            <template #default="{ data }">
              <span>{{ data.name }}</span>
              <span class="sub-count">{{ data.subCount }}</span>
            </template>
          </el-tree>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <div class="page-toolbar">
            <el-input
              v-model="query.keyword"
              :placeholder="t('org.namePlaceholder')"
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
            <el-button v-perm="'orgs:create'" type="primary" @click="openCreate">
              {{ t('org.createTitle') }}
            </el-button>
            <el-button v-perm="'orgs:export'" @click="onExport">{{ t('common.export') }}</el-button>
          </div>
          <div class="table-hint">
            {{ scopeLabel }}{{ t('common.records', { total: page.total }) }}
          </div>
          <el-table :data="page.list" v-loading="loading" border>
            <el-table-column prop="name" :label="t('org.name')" min-width="160" />
            <el-table-column prop="parentName" :label="t('org.parent')" min-width="140" />
            <el-table-column prop="orgTypeLabel" :label="t('org.type')" width="120" />
            <el-table-column prop="contactName" :label="t('org.contactName')" width="110" />
            <el-table-column prop="contactPhone" :label="t('org.contactPhone')" width="140" />
            <el-table-column prop="memberCount" :label="t('org.memberCount')" width="80" />
            <el-table-column prop="subOrgCount" :label="t('org.subCount')" width="80" />
            <el-table-column :label="t('common.status')" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
                  {{ t(`enums.enableStatus.${row.status}`) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('common.action')" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-perm="'orgs:edit'" link type="primary" @click="openEdit(row)">
                  {{ t('common.edit') }}
                </el-button>
                <el-button
                  v-perm="'orgs:toggle'"
                  link
                  type="primary"
                  :disabled="row.orgType !== 'BRANCH'"
                  @click="onToggle(row)"
                >
                  {{ row.status === 'ENABLED' ? t('common.disable') : t('common.enable') }}
                </el-button>
                <el-button
                  v-perm="'orgs:delete'"
                  link
                  type="danger"
                  :disabled="row.orgType !== 'BRANCH'"
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
      </el-col>
    </el-row>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? t('org.editTitle') : t('org.createTitle')"
      width="560px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" :validate-on-rule-change="false" label-width="110px">
        <el-form-item v-if="!editing" :label="t('org.parent')" prop="parentId">
          <OrgTreeSelect
            v-model="form.parentId"
            only-enabled
            :placeholder="t('org.parentPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('org.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="t('org.contactName')">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item :label="t('org.contactPhone')">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <template v-if="!editing">
          <el-divider content-position="left">{{ t('org.adminSection') }}</el-divider>
          <el-form-item :label="t('org.adminAccount')" prop="adminAccount">
            <el-input v-model="form.adminAccount" :placeholder="t('org.adminAccountPlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('org.adminNickname')">
            <el-input v-model="form.adminNickname" />
          </el-form-item>
          <el-form-item :label="t('org.adminRoles')" prop="adminRoleIds">
            <el-select v-model="form.adminRoleIds" multiple style="width: 100%">
              <el-option
                v-for="role in roleOptions"
                :key="role.id"
                :label="role.name"
                :value="role.id"
                :disabled="role.assignable === false"
              />
            </el-select>
          </el-form-item>
          <p class="form-tip">{{ t('org.inviteTip') }}</p>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <InitialPasswordDialog
      :visible="created.visible"
      :account="created.account"
      :password="created.password"
      @close="created.visible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import OrgTreeSelect from '@/components/OrgTreeSelect.vue'
import InitialPasswordDialog from '@/components/InitialPasswordDialog.vue'
import { ENABLE_STATUS } from '@/stores/dict'
import { roleOptions as fetchRoleOptions } from '@/api/role'
import {
  createOrg,
  deleteOrg,
  exportOrgs,
  fetchOrgTree,
  pageOrgs,
  toggleOrg,
  updateOrg,
  type OrgQuery
} from '@/api/org'
import type { Id, OrgItem, OrgTreeNode, RoleOption } from '@/api/types'

const { t } = useI18n()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editing = ref<OrgItem | null>(null)
const treeRef = ref()
const formRef = ref<FormInstance>()
const tree = ref<OrgTreeNode[]>([])
const roleOptions = ref<RoleOption[]>([])
const page = reactive({ total: 0, list: [] as OrgItem[] })
const created = reactive({ visible: false, account: '', password: '' })

const query = reactive<OrgQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: undefined,
  orgId: undefined
})

const selectedNode = ref<OrgTreeNode | null>(null)
const scopeLabel = computed(() =>
  selectedNode.value ? t('org.scopePrefix', { name: selectedNode.value.name }) : ''
)

const form = reactive({
  parentId: undefined as Id | undefined,
  name: '',
  contactName: '',
  contactPhone: '',
  adminAccount: '',
  adminNickname: '',
  adminRoleIds: [] as Id[]
})

const rules = computed<FormRules>(() => ({
  parentId: [{ required: true, message: t('org.parentRequired'), trigger: 'change' }],
  name: [{ required: true, message: t('org.nameRequired'), trigger: 'blur' }],
  adminAccount: [{ required: true, message: t('org.adminAccountRequired'), trigger: 'blur' }],
  adminRoleIds: [{ required: true, message: t('org.rolesRequired'), trigger: 'change' }]
}))

watch(
  () => form.parentId,
  async (orgId) => {
    roleOptions.value = orgId ? await fetchRoleOptions(orgId) : []
  }
)

onMounted(async () => {
  tree.value = await fetchOrgTree()
  await load(1)
})

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  loading.value = true
  try {
    const result = await pageOrgs(query)
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

function onNodeClick(node: OrgTreeNode) {
  if (selectedNode.value?.id === node.id) {
    selectedNode.value = null
    query.orgId = undefined
    treeRef.value?.setCurrentKey(null)
  } else {
    selectedNode.value = node
    query.orgId = node.id
  }
  void load(1)
}

function openCreate() {
  editing.value = null
  Object.assign(form, {
    parentId: selectedNode.value?.id,
    name: '',
    contactName: '',
    contactPhone: '',
    adminAccount: '',
    adminNickname: '',
    adminRoleIds: []
  })
  dialogVisible.value = true
}

function openEdit(row: OrgItem) {
  editing.value = row
  Object.assign(form, {
    parentId: row.parentId,
    name: row.name,
    contactName: row.contactName ?? '',
    contactPhone: row.contactPhone ?? '',
    adminAccount: '',
    adminNickname: '',
    adminRoleIds: []
  })
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateOrg(editing.value.id, {
        name: form.name,
        contactName: form.contactName,
        contactPhone: form.contactPhone
      })
      ElMessage.success(t('common.saved'))
    } else {
      const result = await createOrg({ ...form })
      if (result.initialPassword) {
        created.account = result.adminAccount ?? ''
        created.password = result.initialPassword
        created.visible = true
      }
      ElMessage.success(t('org.created'))
    }
    dialogVisible.value = false
    tree.value = await fetchOrgTree()
    await load(1)
  } finally {
    saving.value = false
  }
}

async function onToggle(row: OrgItem) {
  const target = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  if (target === 'DISABLED') {
    await ElMessageBox.confirm(t('org.disableConfirm'), t('org.disableTitle'))
  }
  await toggleOrg(row.id, target)
  ElMessage.success(t('common.statusUpdated'))
  await load()
}

async function onDelete(row: OrgItem) {
  await ElMessageBox.confirm(t('org.deleteConfirm', { name: row.name }), t('org.deleteTitle'), {
    type: 'warning'
  })
  await deleteOrg(row.id)
  ElMessage.success(t('common.deleted'))
  tree.value = await fetchOrgTree()
  await load(1)
}

async function onExport() {
  const url = await exportOrgs(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.sub-count {
  margin-left: 8px;
  color: #66778d;
  font-size: 12px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
