<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="账号或 IP" clearable style="width: 200px" />
        <el-select v-model="query.result" placeholder="登录结果" clearable style="width: 140px">
          <el-option v-for="item in LOGIN_RESULT" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.entry" placeholder="登录入口" clearable style="width: 140px">
          <el-option v-for="item in SESSION_ENTRY" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-button v-perm="'logins:export'" @click="onExport">导出</el-button>
      </div>
      <div class="table-hint">登录日志只读保留历史，不提供操作入口，也不反映当前是否在线。</div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column prop="account" label="账号" min-width="140" />
        <el-table-column prop="entryLabel" label="入口" width="120" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'">{{ row.resultLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="失败原因" min-width="140">
          <template #default="{ row }">{{ row.failReason || '—' }}</template>
        </el-table-column>
        <el-table-column prop="clientIp" label="IP" width="140" />
        <el-table-column prop="userAgent" label="浏览器／系统" min-width="200" show-overflow-tooltip />
        <el-table-column label="登录时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.loginTime) }}</template>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { LOGIN_RESULT, SESSION_ENTRY } from '@/stores/dict'
import { exportLoginLogs, pageLoginLogs, type LoginLogQuery } from '@/api/log'
import type { LoginLogItem } from '@/api/types'
import { formatDateTime, toUtcIso } from '@/utils/datetime'

const loading = ref(false)
const range = ref<[Date, Date] | null>(null)
const page = reactive({ total: 0, list: [] as LoginLogItem[] })

const query = reactive<LoginLogQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  result: undefined,
  entry: undefined
})

onMounted(() => load(1))

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  query.startTime = toUtcIso(range.value?.[0])
  query.endTime = toUtcIso(range.value?.[1])
  loading.value = true
  try {
    const result = await pageLoginLogs(query)
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
  query.result = undefined
  query.entry = undefined
  range.value = null
  void load(1)
}

async function onExport() {
  const url = await exportLoginLogs(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
