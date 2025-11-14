<template>
  <div class="favorites">
    <div class="toolbar">
      <el-button text class="back-btn" @click="goBack">← 返回</el-button>
      <div class="title">宝贝收藏</div>
      <div class="spacer"></div>
      <div class="count">共 {{ total }} 件</div>
    </div>

    <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
    <el-empty v-else-if="items.length===0" description="暂无收藏的商品" />
    <div v-else class="items-grid">
      <div v-for="item in items" :key="item.id" class="item-card" @click="goToItem(item.id)">
        <div class="item-image">
          <img :src="item.image || placeholder(item.id)" :alt="item.title" />
          <div class="favorite-badge" @click.stop="toggleFavorite(item)">
            <el-icon :size="20" :color="'#ff4d4f'"><StarFilled /></el-icon>
          </div>
        </div>
        <div class="item-info">
          <div class="item-title">{{ item.title }}</div>
          <div class="item-desc">{{ item.description }}</div>
          <div class="item-footer">
            <span class="item-price">¥{{ item.price }}</span>
            <span class="item-status">{{ statusText(item.status) }}</span>
          </div>
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
import { StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const items = ref<any[]>([])
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

async function load() {
  loading.value = true
  const offset = (page.value - 1) * pageSize.value
  try {
    const { data } = await http.get('/api/favorites/items', {
      params: { limit: pageSize.value, offset }
    })
    if (data.code === 0) {
      items.value = data.data?.items || []
      total.value = data.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载失败')
  }
  loading.value = false
}

async function toggleFavorite(item: any) {
  try {
    const { data } = await http.post(`/api/favorites/items/${item.id}`)
    if (data.code === 0) {
      if (!data.data?.isFavorited) {
        ElMessage.success('已取消收藏')
        load()
      } else {
        ElMessage.success('已添加收藏')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
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
.favorites {
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

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.item-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.item-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-badge:hover {
  background: #fff;
  transform: scale(1.1);
}

.item-info {
  padding: 12px;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 32px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 700;
}

.item-status {
  font-size: 12px;
  color: #666;
  padding: 2px 8px;
  background: #f5f5f5;
  border-radius: 4px;
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

