<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input
          v-model="query.keyword"
          :placeholder="t('customer.namePlaceholder')"
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
        <el-button v-perm="'customers:create'" type="primary" @click="openCreate">
          {{ t('customer.createTitle') }}
        </el-button>
        <el-button v-perm="'customers:export'" @click="onExport">{{ t('common.export') }}</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="name" :label="t('customer.name')" min-width="160" />
        <el-table-column prop="contactName" :label="t('customer.contactName')" width="110" />
        <el-table-column prop="contactPhone" :label="t('customer.contactPhone')" width="140" />
        <el-table-column :label="t('customer.region')" min-width="160">
          <template #default="{ row }">
            {{ [row.country, row.province, row.city].filter(Boolean).join(' / ') || t('common.dash') }}
          </template>
        </el-table-column>
        <el-table-column :label="t('customer.models')" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="model in row.modelNames" :key="model" size="small" class="tag">
              {{ model }}
            </el-tag>
            <span v-if="!row.modelNames?.length">{{ t('common.dash') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ t(`enums.enableStatus.${row.status}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.action')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'customers:edit'" link type="primary" @click="openEdit(row)">
              {{ t('common.edit') }}
            </el-button>
            <el-button v-perm="'customers:authorize'" link type="primary" @click="openFeatures(row)">
              {{ t('customer.featureTitle') }}
            </el-button>
            <el-button v-perm="'customers:toggle'" link type="primary" @click="onToggle(row)">
              {{ row.status === 'ENABLED' ? t('common.disable') : t('common.enable') }}
            </el-button>
            <el-button v-perm="'customers:delete'" link type="danger" @click="onDelete(row)">
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
      :title="editing ? t('customer.editTitle') : t('customer.createTitle')"
      width="680px"
      top="6vh"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('customer.name')" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('customer.authCodeChannel')">
              <el-select v-model="form.authCodeChannel" style="width: 100%">
                <el-option
                  v-for="item in AUTH_CODE_CHANNEL"
                  :key="item.value"
                  :label="t(item.labelKey)"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('customer.contactName')">
              <el-input v-model="form.contactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('customer.contactPhone')">
              <el-input v-model="form.contactPhone" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('customer.country')">
              <el-input v-model="form.country" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('customer.province')">
              <el-input v-model="form.province" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('customer.city')">
              <el-input v-model="form.city" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('customer.models')">
              <el-select v-model="form.modelIds" multiple style="width: 100%">
                <el-option
                  v-for="model in models"
                  :key="model.id"
                  :label="t('common.parenthesized', { main: model.model, sub: model.productName })"
                  :value="model.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('common.remark')">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="!editing">
          <el-divider content-position="left">{{ t('customer.adminSection') }}</el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item :label="t('customer.adminAccount')" prop="adminAccount">
                <el-input v-model="form.adminAccount" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="t('customer.adminNickname')">
                <el-input v-model="form.adminNickname" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-divider content-position="left">{{ t('customer.featureSection') }}</el-divider>
          <p class="form-tip">{{ t('customer.featureFormTip') }}</p>
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
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="featureVisible" :title="t('customer.featureTitle')" width="560px">
      <p class="form-tip">{{ t('customer.featureTip') }}</p>
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
        <el-button @click="featureVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitFeatures">{{ t('customer.featureSave') }}</el-button>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
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

const { t } = useI18n()
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

const rules = computed<FormRules>(() => ({
  name: [{ required: true, message: t('customer.nameRequired'), trigger: 'blur' }],
  adminAccount: [{ required: true, message: t('customer.adminAccountRequired'), trigger: 'blur' }]
}))

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
      ElMessage.success(t('common.saved'))
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
  const parts = [t('customer.featureSaved')]
  if (result.added?.length) {
    parts.push(t('customer.featureAdded', { count: result.added.length }))
  }
  if (result.removed?.length) {
    parts.push(t('customer.featureRemoved', { count: result.removed.length }))
  }
  ElMessage.success(parts.join(' · '))
  await load()
}

async function onToggle(row: TenantItem) {
  const target = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  if (target === 'DISABLED') {
    await ElMessageBox.confirm(t('customer.disableConfirm'), t('customer.disableTitle'))
  }
  await toggleTenant(row.id, target)
  ElMessage.success(t('common.statusUpdated'))
  await load()
}

async function onDelete(row: TenantItem) {
  await ElMessageBox.confirm(
    t('customer.deleteConfirm', { name: row.name }),
    t('customer.deleteTitle'),
    { type: 'warning' }
  )
  await deleteTenant(row.id)
  ElMessage.success(t('common.deleted'))
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
