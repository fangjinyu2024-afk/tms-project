<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="客户名称" clearable style="width: 200px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
          <el-option v-for="item in ENABLE_STATUS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="grow" />
        <el-button v-perm="'customers:create'" type="primary" @click="openCreate">新增客户</el-button>
        <el-button v-perm="'customers:export'" @click="onExport">导出</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="name" label="客户名称" min-width="160" />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column label="国家/省/市" min-width="160">
          <template #default="{ row }">
            {{ [row.country, row.province, row.city].filter(Boolean).join(' / ') || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="关联型号" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="model in row.modelNames" :key="model" size="small" class="tag">{{ model }}</el-tag>
            <span v-if="!row.modelNames?.length">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'customers:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-perm="'customers:authorize'" link type="primary" @click="openFeatures(row)">
              功能授权
            </el-button>
            <el-button v-perm="'customers:toggle'" link type="primary" @click="onToggle(row)">
              {{ row.status === 'ENABLED' ? '停用' : '启用' }}
            </el-button>
            <el-button v-perm="'customers:delete'" link type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑客户' : '新增客户'" width="680px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授权码渠道">
              <el-select v-model="form.authCodeChannel" style="width: 100%">
                <el-option v-for="item in AUTH_CODE_CHANNEL" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="国家／地区">
              <el-input v-model="form.country" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="省／州">
              <el-input v-model="form.province" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市">
              <el-input v-model="form.city" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联型号">
              <el-select v-model="form.modelIds" multiple style="width: 100%">
                <el-option
                  v-for="model in models"
                  :key="model.id"
                  :label="`${model.model}（${model.productName}）`"
                  :value="model.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="!editing">
          <el-divider content-position="left">客户管理员</el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="管理员账号" prop="adminAccount">
                <el-input v-model="form.adminAccount" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="管理员昵称">
                <el-input v-model="form.adminNickname" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-divider content-position="left">功能授权</el-divider>
          <p class="form-tip">工作台强制包含；平台专属菜单不出现在可选项中。</p>
          <el-checkbox-group v-model="form.menuKeys">
            <el-checkbox
              v-for="option in featureOptions"
              :key="option.menuKey"
              :value="option.menuKey"
              :disabled="option.required"
            >
              {{ option.menuName }}
            </el-checkbox>
          </el-checkbox-group>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="featureVisible" title="功能授权" width="560px">
      <p class="form-tip">
        客户可用功能是上限，成员实际权限还需角色授权；关闭功能保留业务数据与角色配置，再次开通后自动恢复。
      </p>
      <el-checkbox-group v-model="featureSelection">
        <el-checkbox
          v-for="option in featureOptions"
          :key="option.menuKey"
          :value="option.menuKey"
          :disabled="option.required"
        >
          {{ option.groupName }} · {{ option.menuName }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="featureVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeatures">保存授权</el-button>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import InitialPasswordDialog from '@/components/InitialPasswordDialog.vue'
import { AUTH_CODE_CHANNEL, ENABLE_STATUS } from '@/stores/dict'
import { modelOptions } from '@/api/product'
import {
  createTenant,
  deleteTenant,
  exportTenants,
  fetchFeatureOptions,
  fetchFeatures,
  pageTenants,
  saveFeatures,
  tenantDetail,
  toggleTenant,
  updateTenant,
  type TenantQuery
} from '@/api/tenant'
import type { Id, ModelOption, TenantFeature, TenantItem } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const featureVisible = ref(false)
const editing = ref<TenantItem | null>(null)
const featureTarget = ref<TenantItem | null>(null)
const featureSelection = ref<string[]>([])
const featureOptions = ref<TenantFeature['options']>([])
const models = ref<ModelOption[]>([])
const formRef = ref<FormInstance>()
const page = reactive({ total: 0, list: [] as TenantItem[] })
const created = reactive({ visible: false, account: '', password: '' })

const query = reactive<TenantQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: undefined
})

const form = reactive({
  name: '',
  contactName: '',
  contactPhone: '',
  country: '',
  province: '',
  city: '',
  remark: '',
  authCodeChannel: 'TOOL',
  modelIds: [] as Id[],
  adminAccount: '',
  adminNickname: '',
  menuKeys: ['home'] as string[]
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  adminAccount: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }]
}

onMounted(async () => {
  models.value = await modelOptions()
  featureOptions.value = await fetchFeatureOptions()
  await load(1)
})

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  loading.value = true
  try {
    const result = await pageTenants(query)
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
  Object.assign(form, {
    name: '',
    contactName: '',
    contactPhone: '',
    country: '',
    province: '',
    city: '',
    remark: '',
    authCodeChannel: 'TOOL',
    modelIds: [],
    adminAccount: '',
    adminNickname: '',
    menuKeys: ['home']
  })
  if (!featureOptions.value.length) {
    featureOptions.value = await fetchFeatureOptions()
  }
  dialogVisible.value = true
}

async function openEdit(row: TenantItem) {
  const detail = await tenantDetail(row.id)
  editing.value = detail
  Object.assign(form, {
    name: detail.name,
    contactName: detail.contactName ?? '',
    contactPhone: detail.contactPhone ?? '',
    country: detail.country ?? '',
    province: detail.province ?? '',
    city: detail.city ?? '',
    remark: detail.remark ?? '',
    authCodeChannel: detail.authCodeChannel,
    modelIds: detail.modelIds ?? [],
    adminAccount: '',
    adminNickname: '',
    menuKeys: detail.menuKeys ?? ['home']
  })
  dialogVisible.value = true
}

async function openFeatures(row: TenantItem) {
  featureTarget.value = row
  const feature = await fetchFeatures(row.id)
  featureOptions.value = feature.options
  featureSelection.value = feature.menuKeys
  featureVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateTenant(editing.value.id, { ...form })
      ElMessage.success('客户已保存')
    } else {
      const result = await createTenant({ ...form })
      created.account = result.adminAccount ?? ''
      created.password = result.initialPassword ?? ''
      created.visible = true
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function submitFeatures() {
  if (!featureTarget.value) {
    return
  }
  const result = await saveFeatures(featureTarget.value.id, featureSelection.value)
  featureVisible.value = false
  const added = result.added?.length ? `新增 ${result.added.length} 项` : ''
  const removed = result.removed?.length ? `关闭 ${result.removed.length} 项` : ''
  ElMessage.success(`功能授权已保存${added || removed ? `：${[added, removed].filter(Boolean).join('，')}` : ''}`)
  await load()
}

async function onToggle(row: TenantItem) {
  const target = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  if (target === 'DISABLED') {
    await ElMessageBox.confirm(
      '停用后本客户及下级机构成员的登录会话将全部失效，已发布任务不会自动停止，是否继续？',
      '停用客户'
    )
  }
  await toggleTenant(row.id, target)
  ElMessage.success('状态已更新')
  await load()
}

async function onDelete(row: TenantItem) {
  await ElMessageBox.confirm(`确认删除客户「${row.name}」？`, '删除客户', { type: 'warning' })
  await deleteTenant(row.id)
  ElMessage.success('客户已删除')
  await load()
}

async function onExport() {
  const url = await exportTenants(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.tag {
  margin-right: 6px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
