<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="账号、姓名或 IP" clearable style="width: 200px" />
        <el-select v-model="query.entry" placeholder="入口" clearable style="width: 140px">
          <el-option v-for="item in SESSION_ENTRY" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <OrgTreeSelect v-model="query.orgId" placeholder="所属机构" />
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="grow" />
        <el-button v-perm="'sessions:view'" @click="onExport">导出</el-button>
      </div>
      <div class="table-hint">
        每行一个有效登录会话，同一账号可有多条；强制下线一次只结束一条会话，不等于停用账号。
      </div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column label="账号" min-width="160">
          <template #default="{ row }">
            {{ row.account }}
            <el-tag v-if="row.current" size="small" type="warning" class="tag">当前会话</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="姓名" width="120" />
        <el-table-column prop="orgName" label="机构" min-width="140" />
        <el-table-column prop="entryLabel" label="入口" width="110" />
        <el-table-column prop="clientIp" label="IP" width="140" />
        <el-table-column prop="userAgent" label="浏览器／客户端" min-width="180" show-overflow-tooltip />
        <el-table-column label="登录时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.loginTime) }}</template>
        </el-table-column>
        <el-table-column label="最近活动时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.lastActiveTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <span v-if="row.current" class="form-tip">—</span>
            <el-button v-else v-perm="'sessions:force'" link type="danger" @click="openForce(row)">
              强制下线
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

    <el-dialog v-model="forceVisible" title="强制下线" width="460px">
      <p class="form-tip">
        下线后该会话立即失效，后续请求需要重新登录；账号本身仍可再次登录。
      </p>
      <el-input v-model="reason" type="textarea" :rows="3" placeholder="请填写下线原因（必填）" />
      <template #footer>
        <el-button @click="forceVisible = false">取消</el-button>
        <el-button type="danger" @click="submitForce">确认下线</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import OrgTreeSelect from '@/components/OrgTreeSelect.vue'
import { SESSION_ENTRY } from '@/stores/dict'
import { exportSessions, forceLogout, pageSessions, type SessionQuery } from '@/api/log'
import type { SessionItem } from '@/api/types'
import { formatDateTime } from '@/utils/datetime'

const loading = ref(false)
const forceVisible = ref(false)
const reason = ref('')
const target = ref<SessionItem | null>(null)
const page = reactive({ total: 0, list: [] as SessionItem[] })

const query = reactive<SessionQuery & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  entry: undefined,
  orgId: undefined
})

onMounted(() => load(1))

async function load(pageNum = query.pageNum) {
  query.pageNum = pageNum
  loading.value = true
  try {
    const result = await pageSessions(query)
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
  query.entry = undefined
  query.orgId = undefined
  void load(1)
}

function openForce(row: SessionItem) {
  target.value = row
  reason.value = ''
  forceVisible.value = true
}

async function submitForce() {
  if (!target.value) {
    return
  }
  if (!reason.value.trim()) {
    ElMessage.warning('请填写下线原因')
    return
  }
  await forceLogout(target.value.id, reason.value.trim())
  forceVisible.value = false
  ElMessage.success('该会话已下线')
  await load()
}

async function onExport() {
  const url = await exportSessions(query)
  window.open(url, '_blank')
}
</script>

<style scoped>
.tag {
  margin-left: 6px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
