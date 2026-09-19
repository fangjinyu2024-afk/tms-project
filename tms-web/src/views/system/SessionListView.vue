<template>
  <div class="page">
    <el-card>
      <div class="page-toolbar">
        <el-input
          v-model="query.keyword"
          :placeholder="t('session.keywordPlaceholder')"
          clearable
          style="width: 200px"
        />
        <el-select
          v-model="query.entry"
          :placeholder="t('session.entry')"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in SESSION_ENTRY"
            :key="item.value"
            :label="t(item.labelKey)"
            :value="item.value"
          />
        </el-select>
        <OrgTreeSelect v-model="query.orgId" :placeholder="t('member.orgPlaceholder')" />
        <el-button type="primary" @click="load(1)">{{ t('common.search') }}</el-button>
        <el-button @click="reset">{{ t('common.reset') }}</el-button>
        <span class="grow" />
        <el-button v-perm="'sessions:view'" @click="onExport">{{ t('common.export') }}</el-button>
      </div>
      <div class="table-hint">{{ t('session.listTip') }}</div>

      <el-table :data="page.list" v-loading="loading" border>
        <el-table-column :label="t('session.account')" min-width="160">
          <template #default="{ row }">
            {{ row.account }}
            <el-tag v-if="row.current" size="small" type="warning" class="tag">
              {{ t('session.current') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" :label="t('session.nickname')" width="120" />
        <el-table-column prop="orgName" :label="t('session.org')" min-width="140" />
        <el-table-column prop="entryLabel" :label="t('session.entry')" width="110" />
        <el-table-column prop="clientIp" :label="t('session.clientIp')" width="140" />
        <el-table-column
          prop="userAgent"
          :label="t('session.userAgent')"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column :label="t('session.loginTime')" width="180">
          <template #default="{ row }">{{ formatDateTime(row.loginTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('session.lastActiveTime')" width="180">
          <template #default="{ row }">{{ formatDateTime(row.lastActiveTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.action')" width="120" fixed="right">
          <template #default="{ row }">
            <span v-if="row.current" class="form-tip">{{ t('common.dash') }}</span>
            <el-button v-else v-perm="'sessions:force'" link type="danger" @click="openForce(row)">
              {{ t('session.forceLogout') }}
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

    <el-dialog v-model="forceVisible" :title="t('session.forceLogout')" width="460px">
      <p class="form-tip">{{ t('session.forceTip') }}</p>
      <el-input
        v-model="reason"
        type="textarea"
        :rows="3"
        :placeholder="t('session.reasonPlaceholder')"
      />
      <template #footer>
        <el-button @click="forceVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="danger" @click="submitForce">{{ t('session.forceConfirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import OrgTreeSelect from '@/components/OrgTreeSelect.vue'
import { SESSION_ENTRY } from '@/stores/dict'
import { exportSessions, forceLogout, pageSessions, type SessionQuery } from '@/api/log'
import type { SessionItem } from '@/api/types'
import { formatDateTime } from '@/utils/datetime'

const { t } = useI18n()
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
    ElMessage.warning(t('session.reasonRequired'))
    return
  }
  await forceLogout(target.value.id, reason.value.trim())
  forceVisible.value = false
  ElMessage.success(t('session.forced'))
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
