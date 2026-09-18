<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card>
          <template #header>
            <div>机构目录</div>
            <div class="form-tip">点击节点只看该机构及下级，再次点击恢复全部</div>
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
            <el-input v-model="query.keyword" placeholder="机构名称" clearable style="width: 200px" />
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
              <el-option v-for="item in ENABLE_STATUS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" @click="load(1)">查询</el-button>
            <el-button @click="reset">重置</el-button>
            <span class="grow" />
            <el-button v-perm="'orgs:create'" type="primary" @click="openCreate">新增机构</el-button>
            <el-button v-perm="'orgs:export'" @click="onExport">导出</el-button>
          </div>
          <div class="table-hint">{{ scopeLabel }}{{ page.total }} 条记录</div>
          <el-table :data="page.list" v-loading="loading" border>
            <el-table-column prop="name" label="机构名称" min-width="160" />
            <el-table-column prop="parentName" label="上级机构" min-width="140" />
            <el-table-column prop="orgTypeLabel" label="机构类型" width="120" />
            <el-table-column prop="contactName" label="联系人" width="110" />
            <el-table-column prop="contactPhone" label="联系方式" width="140" />
            <el-table-column prop="memberCount" label="成员" width="80" />
            <el-table-column prop="subOrgCount" label="下级" width="80" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
                  {{ row.status === 'ENABLED' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-perm="'orgs:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button
                  v-perm="'orgs:toggle'"
                  link
                  type="primary"
                  :disabled="row.orgType !== 'BRANCH'"
                  @click="onToggle(row)"
                >
                  {{ row.status === 'ENABLED' ? '停用' : '启用' }}
                </el-button>
                <el-button
                  v-perm="'orgs:delete'"
                  link
                  type="danger"
                  :disabled="row.orgType !== 'BRANCH'"
                  @click="onDelete(row)"
                >
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
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑机构' : '新增机构'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="!editing" label="上级机构" prop="parentId">
          <OrgTreeSelect v-model="form.parentId" only-enabled placeholder="只能选择可管理且已启用的机构" />
        </el-form-item>
        <el-form-item label="机构名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <template v-if="!editing">
          <el-divider content-position="left">机构管理员</el-divider>
          <el-form-item label="管理员账号" prop="adminAccount">
            <el-input v-model="form.adminAccount" placeholder="3 至 64 位字母、数字、点、下划线或短横线" />
          </el-form-item>
          <el-form-item label="管理员昵称">
            <el-input v-model="form.adminNickname" />
          </el-form-item>
          <el-form-item label="管理员角色" prop="adminRoleIds">
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
          <p class="form-tip">本期不提供邮件邀请，统一走直接创建加系统随机密码加首登强制改密。</p>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
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
const scopeLabel = computed(() => (selectedNode.value ? `${selectedNode.value.name}及下级 · ` : ''))

const form = reactive({
  parentId: undefined as Id | undefined,
  name: '',
  contactName: '',
  contactPhone: '',
  adminAccount: '',
  adminNickname: '',
  adminRoleIds: [] as Id[]
})

const rules: FormRules = {
  parentId: [{ required: true, message: '请选择上级机构', trigger: 'change' }],
  name: [{ required: true, message: '请输入机构名称', trigger: 'blur' }],
  adminAccount: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  adminRoleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change' }]
}

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
      ElMessage.success('机构已保存')
    } else {
      const result = await createOrg({ ...form })
      if (result.initialPassword) {
        created.account = result.adminAccount ?? ''
        created.password = result.initialPassword
        created.visible = true
      }
      ElMessage.success('机构已创建')
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
    await ElMessageBox.confirm('停用后本机构及下级成员的登录会话将全部失效，是否继续？', '停用机构')
  }
  await toggleOrg(row.id, target)
  ElMessage.success('状态已更新')
  await load()
}

async function onDelete(row: OrgItem) {
  await ElMessageBox.confirm(`确认删除机构「${row.name}」？`, '删除机构', { type: 'warning' })
  await deleteOrg(row.id)
  ElMessage.success('机构已删除')
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
