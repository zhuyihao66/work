<template>
  <div class="favorite-shops">
    <div class="toolbar">
      <el-button text class="back-btn" @click="goBack">← 返回</el-button>
      <div class="title">收藏的店</div>
      <div class="spacer"></div>
      <div class="count">共 {{ total }} 家</div>
    </div>

    <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
    <el-empty v-else-if="stores.length===0" description="暂无收藏的店铺" />
    <div v-else class="stores-grid">
      <div v-for="store in stores" :key="store.id" class="store-card" @click="goToStore(store.id)">
        <div class="store-header">
          <el-avatar :size="64" :src="store.avatarUrl">
            {{ store.displayName?.slice(0, 1).toUpperCase() || 'S' }}
          </el-avatar>
          <div class="favorite-badge" @click.stop="toggleFavorite(store)">
            <el-icon :size="20" :color="'#ff4d4f'"><StarFilled /></el-icon>
          </div>
        </div>
        <div class="store-info">
          <div class="store-name">{{ store.displayName }}</div>
          <div class="store-meta">
            <span>在售商品：{{ store.itemCount }} 件</span>
          </div>
          <div class="store-footer">
            <el-button size="small" type="primary" @click.stop="goToStore(store.id)">进入店铺</el-button>
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
const stores = ref<any[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(true)

async function load() {
  loading.value = true
  const offset = (page.value - 1) * pageSize.value
  try {
    const { data } = await http.get('/api/favorites/stores', {
      params: { limit: pageSize.value, offset }
    })
    if (data.code === 0) {
      stores.value = data.data?.stores || []
      total.value = data.data?.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载失败')
  }
  loading.value = false
}

async function toggleFavorite(store: any) {
  try {
    const { data } = await http.post(`/api/favorites/stores/${store.id}`)
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

function goToStore(sellerId: number) {
  // 跳转到该卖家的店铺页面
  router.push(`/store/${sellerId}`)
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

onMounted(load)
</script>

<style scoped>
.favorite-shops {
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

.stores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.store-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.store-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.store-header {
  position: relative;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.favorite-badge {
  position: absolute;
  top: 12px;
  right: 12px;
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

.store-info {
  padding: 16px;
  text-align: center;
}

.store-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.store-meta {
  font-size: 12px;
  color: #666;
  margin-bottom: 12px;
}

.store-footer {
  margin-top: 12px;
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

