<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="产品名称" clearable style="width: 200px" />
        <el-select v-model="query.category" placeholder="产品类别" clearable style="width: 160px">
          <el-option v-for="item in PRODUCT_CATEGORY" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="grow" />
        <el-button v-perm="'products:create'" type="primary" @click="openCreate">新增产品</el-button>
        <el-button v-perm="'products:export'" @click="onExport">导出</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="name" label="产品名称" min-width="160" />
        <el-table-column prop="categoryLabel" label="产品类别" width="120" />
        <el-table-column label="型号" min-width="220">
          <template #default="{ row }">
            <el-tag v-for="model in row.models" :key="model.id" size="small" class="tag">{{ model.model }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'products:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-perm="'products:delete'" link type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑产品' : '新增产品'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="产品类别" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="item in PRODUCT_CATEGORY" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="图片路径">
          <el-input v-model="form.imagePath" placeholder="对象存储路径，选填" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="型号">
          <div class="models">
            <div v-for="(model, index) in form.models" :key="index" class="model-row">
              <el-input v-model="model.model" placeholder="型号标识，平台内唯一" />
              <el-button link type="danger" @click="form.models.splice(index, 1)">移除</el-button>
            </div>
            <el-button link type="primary" @click="form.models.push({ model: '' })">添加型号</el-button>
            <p class="form-tip">已被设备、客户授权或升级内容引用的型号不能移除。</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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

const rules: FormRules = {
  category: [{ required: true, message: '请选择产品类别', trigger: 'change' }],
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }]
}

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
    ElMessage.warning('请至少添加一个型号')
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
    ElMessage.success('产品已保存')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: ProductItem) {
  await ElMessageBox.confirm(`确认删除产品「${row.name}」？`, '删除产品', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('产品已删除')
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
