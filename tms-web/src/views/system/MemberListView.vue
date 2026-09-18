<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="账号或昵称" clearable style="width: 200px" />
        <OrgTreeSelect v-model="query.orgId" placeholder="所属机构" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
          <el-option v-for="item in ENABLE_STATUS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="grow" />
        <el-button v-perm="'members:create'" type="primary" @click="openCreate">新增成员</el-button>
        <el-button v-perm="'members:export'" @click="onExport">导出</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="account" label="登录账号" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="orgName" label="所属机构" min-width="140" />
        <el-table-column label="角色" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" class="role-tag" size="small">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">
            <span>{{ row.email || '—' }}</span>
            <el-tag v-if="row.email" size="small" :type="row.emailVerified ? 'success' : 'info'" class="role-tag">
              {{ row.emailVerified ? '已验证' : '未验证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'members:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-perm="'members:assign'" link type="primary" @click="openAssign(row)">分配角色</el-button>
            <el-button v-perm="'members:reset'" link type="primary" @click="onReset(row)">重置密码</el-button>
            <el-button v-perm="'members:toggle'" link type="primary" @click="onToggle(row)">
              {{ row.status === 'ENABLED' ? '停用' : '启用' }}
            </el-button>
            <el-button v-perm="'members:delete'" link type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑成员' : '新增成员'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="!editing" label="所属机构" prop="orgId">
          <OrgTreeSelect v-model="form.orgId" only-enabled />
        </el-form-item>
        <el-form-item v-if="!editing" label="登录账号" prop="account">
          <el-input v-model="form.account" placeholder="创建后不可更改" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="选填，用于通知与自助找回密码" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.name"
              :value="role.id"
              :disabled="role.assignable === false"
            />
          </el-select>
        </el-form-item>
        <p class="form-tip">初始密码由系统生成并在保存后显示一次，成员首次登录必须修改。</p>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="分配角色" width="480px">
      <el-select v-model="assignRoleIds" multiple style="width: 100%">
        <el-option
          v-for="role in roleOptions"
          :key="role.id"
          :label="role.name"
          :value="role.id"
          :disabled="role.assignable === false"
        />
      </el-select>
      <p class="form-tip">不能增减无权分配的角色；成员已有的更高权限角色保持只读。</p>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">保存</el-button>
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
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import OrgTreeSelect from '@/components/OrgTreeSelect.vue'
import InitialPasswordDialog from '@/components/InitialPasswordDialog.vue'
import { ENABLE_STATUS } from '@/stores/dict'
import { roleOptions as fetchRoleOptions } from '@/api/role'
import {
  assignRoles,
  createMember,
  deleteMember,
  exportMembers,
  pageMembers,
  resetMemberPassword,
  toggleMember,
  updateMember,
  type MemberQuery
} from '@/api/member'
import type { Id, MemberItem, RoleOption } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const assignVisible = ref(false)
const editing = ref<MemberItem | null>(null)
const assigning = ref<MemberItem | null>(null)
const assignRoleIds = ref<Id[]>([])
const formRef = ref<FormInstance>()
const roleOptions = ref<RoleOption[]>([])
const page = reactive({ total: 0, list: [] as MemberItem[] })
const created = reactive({ visible: false, account: '', password: '' })

const query = reactive<MemberQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  orgId: undefined,
  status: undefined
})

const form = reactive({
  orgId: undefined as Id | undefined,
  account: '',
  nickname: '',
  email: '',
  phone: '',
  roleIds: [] as Id[]
})

const rules: FormRules = {
  orgId: [{ required: true, message: '请选择所属机构', trigger: 'change' }],
  account: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  roleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change' }]
}

watch(
  () => form.orgId,
  async (orgId) => {
    roleOptions.value = orgId ? await fetchRoleOptions(orgId) : []
  }
)

onMounted(() => load(1))

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  loading.value = true
  try {
    const result = await pageMembers(query)
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
  query.orgId = undefined
  query.status = undefined
  void load(1)
}

function openCreate() {
  editing.value = null
  Object.assign(form, { orgId: query.orgId, account: '', nickname: '', email: '', phone: '', roleIds: [] })
  dialogVisible.value = true
}

function openEdit(row: MemberItem) {
  editing.value = row
  Object.assign(form, {
    orgId: row.orgId,
    account: row.account,
    nickname: row.nickname,
    email: row.email ?? '',
    phone: row.phone ?? '',
    roleIds: row.roles.map((role) => role.id)
  })
  dialogVisible.value = true
}

async function openAssign(row: MemberItem) {
  assigning.value = row
  assignRoleIds.value = row.roles.map((role) => role.id)
  roleOptions.value = await fetchRoleOptions(row.orgId)
  assignVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateMember(editing.value.id, {
        nickname: form.nickname,
        email: form.email,
        phone: form.phone,
        roleIds: form.roleIds
      })
      ElMessage.success('成员已保存')
    } else {
      const result = await createMember({ ...form })
      created.account = result.account ?? ''
      created.password = result.initialPassword ?? ''
      created.visible = true
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function submitAssign() {
  if (!assigning.value) {
    return
  }
  await assignRoles(assigning.value.id, assignRoleIds.value)
  assignVisible.value = false
  ElMessage.success('角色已保存')
  await load()
}

async function onReset(row: MemberItem) {
  await ElMessageBox.confirm(`重置后该成员的登录会话将失效，确认重置「${row.account}」的密码？`, '重置密码')
  const result = await resetMemberPassword(row.id)
  created.account = result.account ?? row.account
  created.password = result.initialPassword ?? ''
  created.visible = true
}

async function onToggle(row: MemberItem) {
  const target = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  if (target === 'DISABLED') {
    await ElMessageBox.confirm('停用后该成员的登录会话将全部失效，是否继续？', '停用成员')
  }
  await toggleMember(row.id, target)
  ElMessage.success('状态已更新')
  await load()
}

async function onDelete(row: MemberItem) {
  await ElMessageBox.confirm(`确认删除成员「${row.account}」？历史操作归属会保留。`, '删除成员', {
    type: 'warning'
  })
  await deleteMember(row.id)
  ElMessage.success('成员已删除')
  await load()
}

async function onExport() {
  const url = await exportMembers(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.role-tag {
  margin-right: 6px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
