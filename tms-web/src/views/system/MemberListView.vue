<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input
          v-model="query.keyword"
          :placeholder="t('member.keywordPlaceholder')"
          clearable
          style="width: 200px"
        />
        <OrgTreeSelect v-model="query.orgId" :placeholder="t('member.orgPlaceholder')" />
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
        <el-button v-perm="'members:create'" type="primary" @click="openCreate">
          {{ t('member.createTitle') }}
        </el-button>
        <el-button v-perm="'members:export'" @click="onExport">{{ t('common.export') }}</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="account" :label="t('member.account')" min-width="140" />
        <el-table-column prop="nickname" :label="t('member.nickname')" min-width="120" />
        <el-table-column prop="orgName" :label="t('member.org')" min-width="140" />
        <el-table-column :label="t('member.roles')" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" class="role-tag" size="small">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="t('member.email')" min-width="180">
          <template #default="{ row }">
            <span>{{ row.email || t('common.dash') }}</span>
            <el-tag
              v-if="row.email"
              size="small"
              :type="row.emailVerified ? 'success' : 'info'"
              class="role-tag"
            >
              {{ row.emailVerified ? t('member.verified') : t('member.unverified') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ t(`enums.enableStatus.${row.status}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.action')" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'members:edit'" link type="primary" @click="openEdit(row)">
              {{ t('common.edit') }}
            </el-button>
            <el-button v-perm="'members:assign'" link type="primary" @click="openAssign(row)">
              {{ t('member.assignRoles') }}
            </el-button>
            <el-button v-perm="'members:reset'" link type="primary" @click="onReset(row)">
              {{ t('member.resetPassword') }}
            </el-button>
            <el-button v-perm="'members:toggle'" link type="primary" @click="onToggle(row)">
              {{ row.status === 'ENABLED' ? t('common.disable') : t('common.enable') }}
            </el-button>
            <el-button v-perm="'members:delete'" link type="danger" @click="onDelete(row)">
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

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? t('member.editTitle') : t('member.createTitle')"
      width="560px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="!editing" :label="t('member.org')" prop="orgId">
          <OrgTreeSelect v-model="form.orgId" only-enabled />
        </el-form-item>
        <el-form-item v-if="!editing" :label="t('member.account')" prop="account">
          <el-input v-model="form.account" :placeholder="t('member.accountPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('member.nickname')" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item :label="t('member.email')">
          <el-input v-model="form.email" :placeholder="t('member.emailPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('member.phone')">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item :label="t('member.roles')" prop="roleIds">
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
        <p class="form-tip">{{ t('member.initialPasswordTip') }}</p>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" :title="t('member.assignRoles')" width="480px">
      <el-select v-model="assignRoleIds" multiple style="width: 100%">
        <el-option
          v-for="role in roleOptions"
          :key="role.id"
          :label="role.name"
          :value="role.id"
          :disabled="role.assignable === false"
        />
      </el-select>
      <p class="form-tip">{{ t('member.assignTip') }}</p>
      <template #footer>
        <el-button @click="assignVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitAssign">{{ t('common.save') }}</el-button>
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

const { t } = useI18n()
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

const rules = computed<FormRules>(() => ({
  orgId: [{ required: true, message: t('member.orgRequired'), trigger: 'change' }],
  account: [{ required: true, message: t('member.accountRequired'), trigger: 'blur' }],
  nickname: [{ required: true, message: t('member.nicknameRequired'), trigger: 'blur' }],
  roleIds: [{ required: true, message: t('member.rolesRequired'), trigger: 'change' }]
}))

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
      ElMessage.success(t('common.saved'))
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
  ElMessage.success(t('member.rolesSaved'))
  await load()
}

async function onReset(row: MemberItem) {
  await ElMessageBox.confirm(
    t('member.resetConfirm', { account: row.account }),
    t('member.resetPassword')
  )
  const result = await resetMemberPassword(row.id)
  created.account = result.account ?? row.account
  created.password = result.initialPassword ?? ''
  created.visible = true
}

async function onToggle(row: MemberItem) {
  const target = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  if (target === 'DISABLED') {
    await ElMessageBox.confirm(t('member.disableConfirm'), t('member.disableTitle'))
  }
  await toggleMember(row.id, target)
  ElMessage.success(t('common.statusUpdated'))
  await load()
}

async function onDelete(row: MemberItem) {
  await ElMessageBox.confirm(
    t('member.deleteConfirm', { account: row.account }),
    t('member.deleteTitle'),
    { type: 'warning' }
  )
  await deleteMember(row.id)
  ElMessage.success(t('common.deleted'))
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
