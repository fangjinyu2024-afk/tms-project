<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="账号、昵称或对象" clearable style="width: 200px" />
        <el-select v-model="query.module" placeholder="业务模块" clearable style="width: 160px">
          <el-option v-for="item in LOG_MODULE" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.action" placeholder="操作类型" clearable style="width: 150px">
          <el-option v-for="item in OPER_ACTION" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.result" placeholder="结果" clearable style="width: 130px">
          <el-option v-for="item in OPER_RESULT" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-date-picker
          v-model="range"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="grow" />
        <el-button v-perm="'logs:export'" @click="onExport">导出</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="moduleLabel" label="业务模块" width="130" />
        <el-table-column prop="actionLabel" label="操作类型" width="110" />
        <el-table-column label="业务对象" min-width="160">
          <template #default="{ row }">{{ row.objectName || row.objectId || '—' }}</template>
        </el-table-column>
        <el-table-column label="结果" width="110">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : row.result === 'PARTIAL' ? 'warning' : 'danger'">
              {{ row.resultLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人" min-width="150">
          <template #default="{ row }">{{ row.nickname }}（{{ row.account }}）</template>
        </el-table-column>
        <el-table-column prop="clientIp" label="来源 IP" width="140" />
        <el-table-column label="操作时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.operTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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

    <el-drawer v-model="detailVisible" title="操作日志详情" size="520px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="业务模块">{{ current.moduleLabel }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ current.actionLabel }}</el-descriptions-item>
        <el-descriptions-item label="业务对象">
          {{ current.objectName || current.objectId || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="结果">{{ current.resultLabel }}</el-descriptions-item>
        <el-descriptions-item v-if="current.failReason" label="失败原因">
          {{ current.failReason }}
        </el-descriptions-item>
        <el-descriptions-item v-if="current.totalCount" label="批量台数">
          {{ current.successCount }} / {{ current.totalCount }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ current.nickname }}（{{ current.account }}）
        </el-descriptions-item>
        <el-descriptions-item label="来源 IP">{{ current.clientIp }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(current.operTime) }}</el-descriptions-item>
        <el-descriptions-item label="关联编号">{{ current.traceId }}</el-descriptions-item>
        <el-descriptions-item label="变更摘要">
          <pre class="summary">{{ prettySummary }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { LOG_MODULE, OPER_ACTION, OPER_RESULT } from '@/stores/dict'
import { exportOperLogs, operLogDetail, pageOperLogs, type OperLogQuery } from '@/api/log'
import type { OperLogItem } from '@/api/types'
import { formatDateTime, toUtcIso } from '@/utils/datetime'

const loading = ref(false)
const detailVisible = ref(false)
const current = ref<OperLogItem | null>(null)
const range = ref<[Date, Date] | null>(null)
const page = reactive({ total: 0, list: [] as OperLogItem[] })

const query = reactive<OperLogQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  module: undefined,
  action: undefined,
  result: undefined
})

const prettySummary = computed(() => {
  if (!current.value?.changeSummary) {
    return '—'
  }
  try {
    return JSON.stringify(JSON.parse(current.value.changeSummary), null, 2)
  } catch {
    return current.value.changeSummary
  }
})

onMounted(() => load(1))

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  query.startTime = toUtcIso(range.value?.[0])
  query.endTime = toUtcIso(range.value?.[1])
  loading.value = true
  try {
    const result = await pageOperLogs(query)
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
  query.module = undefined
  query.action = undefined
  query.result = undefined
  range.value = null
  void load(1)
}

async function openDetail(row: OperLogItem) {
  current.value = await operLogDetail(row.id)
  detailVisible.value = true
}

async function onExport() {
  const url = await exportOperLogs(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.summary {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
