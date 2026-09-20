<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input
          v-model="query.keyword"
          :placeholder="t('product.namePlaceholder')"
          clearable
          style="width: 200px"
        />
        <el-select
          v-model="query.category"
          :placeholder="t('product.category')"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="item in PRODUCT_CATEGORY"
            :key="item.value"
            :label="t(item.labelKey)"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" @click="load(1)">{{ t('common.search') }}</el-button>
        <el-button @click="reset">{{ t('common.reset') }}</el-button>
        <span class="grow" />
        <el-button v-perm="'products:create'" type="primary" @click="openCreate">
          {{ t('product.createTitle') }}
        </el-button>
        <el-button v-perm="'products:export'" @click="onExport">{{ t('common.export') }}</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="name" :label="t('product.name')" min-width="160" />
        <el-table-column prop="categoryLabel" :label="t('product.category')" width="120" />
        <el-table-column :label="t('product.models')" min-width="220">
          <template #default="{ row }">
            <el-tag v-for="model in row.models" :key="model.id" size="small" class="tag">
              {{ model.model }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('product.description')" min-width="200" />
        <el-table-column :label="t('common.action')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'products:edit'" link type="primary" @click="openEdit(row)">
              {{ t('common.edit') }}
            </el-button>
            <el-button v-perm="'products:delete'" link type="danger" @click="onDelete(row)">
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
      :title="editing ? t('product.editTitle') : t('product.createTitle')"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" :validate-on-rule-change="false" label-width="96px">
        <el-form-item :label="t('product.category')" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option
              v-for="item in PRODUCT_CATEGORY"
              :key="item.value"
              :label="t(item.labelKey)"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('product.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="t('product.imagePath')">
          <el-input v-model="form.imagePath" :placeholder="t('product.imagePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('product.description')">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item :label="t('product.models')">
          <div class="models">
            <div v-for="(model, index) in form.models" :key="index" class="model-row">
              <el-input v-model="model.model" :placeholder="t('product.modelPlaceholder')" />
              <el-button link type="danger" @click="form.models.splice(index, 1)">
                {{ t('product.removeModel') }}
              </el-button>
            </div>
            <el-button link type="primary" @click="form.models.push({ model: '' })">
              {{ t('product.addModel') }}
            </el-button>
            <p class="form-tip">{{ t('product.modelTip') }}</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { PRODUCT_CATEGORY } from '@/stores/dict'
import {
  createProduct,
  deleteProduct,
  exportProducts,
  pageProducts,
  productDetail,
  updateProduct,
  type ProductQuery
} from '@/api/product'
import type { Id, ProductItem } from '@/api/types'

const { t } = useI18n()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editing = ref<ProductItem | null>(null)
const formRef = ref<FormInstance>()
const page = reactive({ total: 0, list: [] as ProductItem[] })

const query = reactive<ProductQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  category: undefined
})

const form = reactive({
  category: 'TRADITIONAL_POS',
  name: '',
  imagePath: '',
  description: '',
  models: [{ model: '' }] as Array<{ id?: Id; model: string }>
})

const rules = computed<FormRules>(() => ({
  category: [{ required: true, message: t('product.categoryRequired'), trigger: 'change' }],
  name: [{ required: true, message: t('product.nameRequired'), trigger: 'blur' }]
}))

onMounted(() => load(1))

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  loading.value = true
  try {
    const result = await pageProducts(query)
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
  query.category = undefined
  void load(1)
}

function openCreate() {
  editing.value = null
  Object.assign(form, {
    category: 'TRADITIONAL_POS',
    name: '',
    imagePath: '',
    description: '',
    models: [{ model: '' }]
  })
  dialogVisible.value = true
}

async function openEdit(row: ProductItem) {
  const detail = await productDetail(row.id)
  editing.value = detail
  Object.assign(form, {
    category: detail.category,
    name: detail.name,
    imagePath: detail.imagePath ?? '',
    description: detail.description ?? '',
    models: detail.models.map((model) => ({ id: model.id, model: model.model }))
  })
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  const models = form.models.filter((model) => model.model.trim())
  if (!models.length) {
    ElMessage.warning(t('product.modelRequired'))
    return
  }
  saving.value = true
  try {
    const payload = { ...form, models }
    if (editing.value) {
      await updateProduct(editing.value.id, payload)
    } else {
      await createProduct(payload)
    }
    ElMessage.success(t('common.saved'))
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: ProductItem) {
  await ElMessageBox.confirm(
    t('product.deleteConfirm', { name: row.name }),
    t('product.deleteTitle'),
    { type: 'warning' }
  )
  await deleteProduct(row.id)
  ElMessage.success(t('common.deleted'))
  await load()
}

async function onExport() {
  const url = await exportProducts(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.tag {
  margin-right: 6px;
}

.models {
  width: 100%;
}

.model-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
