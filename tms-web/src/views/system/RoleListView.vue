<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="角色名称" clearable style="width: 200px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
          <el-option v-for="item in ENABLE_STATUS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="grow" />
        <el-button v-perm="'roles:create'" type="primary" @click="openCreate">新增角色</el-button>
        <el-button v-perm="'roles:export'" @click="onExport">导出</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column label="角色名称" min-width="180">
          <template #default="{ row }">
            {{ row.name }}
            <el-tag v-if="row.builtin" size="small" type="warning" class="tag">内置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerOrgName" label="归属机构" min-width="140" />
        <el-table-column prop="dataScopeLabel" label="可管理范围" min-width="180" />
        <el-table-column prop="memberCount" label="成员数" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openView(row)">查看权限</el-button>
            <el-button v-perm="'roles:edit'" link type="primary" :disabled="row.builtin" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button v-perm="'roles:copy'" link type="primary" @click="onCopy(row)">复制</el-button>
            <el-button v-perm="'roles:toggle'" link type="primary" :disabled="row.builtin" @click="onToggle(row)">
              {{ row.status === 'ENABLED' ? '停用' : '启用' }}
            </el-button>
            <el-button v-perm="'roles:delete'" link type="danger" :disabled="row.builtin" @click="onDelete(row)">
              删除
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
            <el-form-item label="角色名称" prop="name">
              <el-input v-model="form.name" :disabled="readonly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可管理范围" prop="dataScope">
              <el-select v-model="form.dataScope" :disabled="readonly" style="width: 100%">
                <el-option v-for="item in DATA_SCOPE" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="!editing" :span="12">
            <el-form-item label="归属机构" prop="ownerOrgId">
              <OrgTreeSelect v-model="form.ownerOrgId" only-enabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="说明">
              <el-input v-model="form.description" :disabled="readonly" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <p class="form-tip">
        按菜单分组平铺展示，行内为该菜单可配置的操作；无权授予的操作置灰，勾选操作时自动补齐查看权限。
      </p>
      <PermissionMatrix
        :catalog="catalog"
        :selected="form.permCodes"
        :disabled="readonly"
        @update:selected="(value) => (form.permCodes = value)"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">{{ readonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!readonly" type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
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

const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择可管理范围', trigger: 'change' }],
  ownerOrgId: [{ required: true, message: '请选择归属机构', trigger: 'change' }]
}

const dialogTitle = computed(() => (readonly.value ? '角色权限' : editing.value ? '编辑角色' : '新增角色'))

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
    ElMessage.success('角色已保存')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onCopy(row: RoleItem) {
  await ElMessageBox.confirm('复制后只带入当前可授予的权限项，是否继续？', '复制角色')
  await copyRole(row.id)
  ElMessage.success('角色已复制')
  await load()
}

async function onToggle(row: RoleItem) {
  await toggleRole(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success('状态已更新')
  await load()
}

async function onDelete(row: RoleItem) {
  await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '删除角色', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('角色已删除')
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
