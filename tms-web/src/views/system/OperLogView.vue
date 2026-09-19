<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input
          v-model="query.keyword"
          :placeholder="t('operLog.keywordPlaceholder')"
          clearable
          style="width: 200px"
        />
        <el-select
          v-model="query.module"
          :placeholder="t('operLog.module')"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="item in LOG_MODULE"
            :key="item.value"
            :label="t(item.labelKey)"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="query.action"
          :placeholder="t('operLog.action')"
          clearable
          style="width: 150px"
        >
          <el-option
            v-for="item in OPER_ACTION"
            :key="item.value"
            :label="t(item.labelKey)"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="query.result"
          :placeholder="t('operLog.result')"
          clearable
          style="width: 130px"
        >
          <el-option
            v-for="item in OPER_RESULT"
            :key="item.value"
            :label="t(item.labelKey)"
            :value="item.value"
          />
        </el-select>
        <el-date-picker
          v-model="range"
          type="datetimerange"
          :range-separator="t('common.to')"
          :start-placeholder="t('common.startTime')"
          :end-placeholder="t('common.endTime')"
        />
        <el-button type="primary" @click="load(1)">{{ t('common.search') }}</el-button>
        <el-button @click="reset">{{ t('common.reset') }}</el-button>
        <span class="grow" />
        <el-button v-perm="'logs:export'" @click="onExport">{{ t('common.export') }}</el-button>
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="moduleLabel" :label="t('operLog.module')" width="130" />
        <el-table-column prop="actionLabel" :label="t('operLog.action')" width="110" />
        <el-table-column :label="t('operLog.object')" min-width="160">
          <template #default="{ row }">
            {{ row.objectName || row.objectId || t('common.dash') }}
          </template>
        </el-table-column>
        <el-table-column :label="t('operLog.result')" width="110">
          <template #default="{ row }">
            <el-tag
              :type="
                row.result === 'SUCCESS' ? 'success' : row.result === 'PARTIAL' ? 'warning' : 'danger'
              "
            >
              {{ row.resultLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('operLog.operator')" min-width="150">
          <template #default="{ row }">
            {{ t('common.parenthesized', { main: row.nickname, sub: row.account }) }}
          </template>
        </el-table-column>
        <el-table-column prop="clientIp" :label="t('operLog.clientIp')" width="140" />
        <el-table-column :label="t('operLog.operTime')" width="180">
          <template #default="{ row }">{{ formatDateTime(row.operTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.action')" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">
              {{ t('common.detail') }}
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

    <el-drawer v-model="detailVisible" :title="t('operLog.detailTitle')" size="520px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item :label="t('operLog.module')">
          {{ current.moduleLabel }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('operLog.action')">
          {{ current.actionLabel }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('operLog.object')">
          {{ current.objectName || current.objectId || t('common.dash') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('operLog.result')">
          {{ current.resultLabel }}
        </el-descriptions-item>
        <el-descriptions-item v-if="current.failReason" :label="t('operLog.failReason')">
          {{ current.failReason }}
        </el-descriptions-item>
        <el-descriptions-item v-if="current.totalCount" :label="t('operLog.batchCount')">
          {{ current.successCount }} / {{ current.totalCount }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('operLog.operator')">
          {{ t('common.parenthesized', { main: current.nickname, sub: current.account }) }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('operLog.clientIp')">
          {{ current.clientIp }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('operLog.operTime')">
          {{ formatDateTime(current.operTime) }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('operLog.traceId')">{{ current.traceId }}</el-descriptions-item>
        <el-descriptions-item :label="t('operLog.changeSummary')">
          <pre class="summary">{{ prettySummary }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { LOG_MODULE, OPER_ACTION, OPER_RESULT } from '@/stores/dict'
import { exportOperLogs, operLogDetail, pageOperLogs, type OperLogQuery } from '@/api/log'
import type { OperLogItem } from '@/api/types'
import { formatDateTime, toUtcIso } from '@/utils/datetime'

const { t } = useI18n()
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
    return t('common.dash')
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
