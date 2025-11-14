<template>
  <div class="footprints">
    <div class="toolbar">
      <el-button text class="back-btn" @click="goBack">← 返回</el-button>
      <div class="title">我的足迹</div>
      <div class="spacer"></div>
      <el-button size="small" type="danger" plain @click="clearAll">清空足迹</el-button>
      <div class="count">共 {{ total }} 条</div>
    </div>

    <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
    <el-empty v-else-if="history.length===0" description="暂无浏览记录" />
    <div v-else class="history-list">
      <div v-for="item in history" :key="item.id" class="history-item">
        <div class="item-image" @click="goToItem(item.id)">
          <img :src="item.image || placeholder(item.id)" :alt="item.title" />
        </div>
        <div class="item-info" @click="goToItem(item.id)">
          <div class="item-title">{{ item.title }}</div>
          <div class="item-desc">{{ item.description }}</div>
          <div class="item-footer">
            <span class="item-price">¥{{ item.price }}</span>
            <span class="item-status">{{ statusText(item.status) }}</span>
          </div>
        </div>
        <div class="item-actions">
          <div class="view-time">{{ formatDate(item.viewedAt) }}</div>
          <el-button size="small" text type="danger" @click="deleteItem(item.id)">删除</el-button>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../api/http'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const history = ref<any[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(true)

function placeholder(seed: any) {
  return `https://picsum.photos/seed/item${seed}/300/200`
}

function statusText(s: string) {
  const map: any = { listed: '在售', reserved: '已预订', sold: '已售出', archived: '已下架' }
  return map[s] || s
}

function formatDate(iso: string) {
  if (!iso) return ''
  const date = new Date(iso)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

async function load() {
  loading.value = true
  const offset = (page.value - 1) * pageSize.value
  try {
    const { data } = await http.get('/api/favorites/history', {
      params: { limit: pageSize.value, offset }
    })
    if (data.code === 0) {
      history.value = data.data?.history || []
      total.value = data.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载失败')
  }
  loading.value = false
}

async function deleteItem(itemId: number) {
  try {
    await http.delete(`/api/favorites/history/${itemId}`)
    ElMessage.success('已删除')
    load()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

async function clearAll() {
  try {
    await ElMessageBox.confirm('确定要清空所有浏览记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await http.delete('/api/favorites/history')
    ElMessage.success('已清空')
    load()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清空失败')
    }
  }
}

function onPage(p: number) {
  page.value = p
  load()
}

function goToItem(id: number) {
  router.push(`/item/${id}`)
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

onMounted(load)
</script>

<style scoped>
.footprints {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.back-btn {
  color: #666;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.spacer {
  flex: 1;
}

.count {
  color: #666;
  font-size: 14px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s;
}

.history-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-image {
  width: 160px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  cursor: pointer;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  cursor: pointer;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.item-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 40px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: 700;
}

.item-status {
  font-size: 12px;
  color: #666;
  padding: 2px 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  min-width: 100px;
}

.view-time {
  font-size: 12px;
  color: #999;
}

.pager {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.spinner {
  width: 26px;
  height: 26px;
  border: 3px solid #e5e5e5;
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

