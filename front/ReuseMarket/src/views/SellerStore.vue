<template>
  <div class="seller-store">
    <div class="toolbar">
      <el-button text class="back-btn" @click="goBack">← 返回</el-button>
      <div class="spacer"></div>
    </div>

    <!-- 店铺信息 -->
    <div v-if="storeInfo" class="store-header">
      <div class="store-avatar">
        <el-avatar :size="80" :src="storeInfo.avatarUrl">
          {{ (storeInfo.displayName || 'S').slice(0, 1).toUpperCase() }}
        </el-avatar>
      </div>
      <div class="store-details">
        <h2 class="store-name">{{ storeInfo.displayName }}</h2>
        <div class="store-meta">
          <span>在售商品：{{ total }} 件</span>
        </div>
      </div>
      <div class="store-actions">
        <el-button 
          :type="isFavorited ? 'warning' : 'primary'"
          :icon="isFavorited ? StarFilled : Star"
          @click="toggleFavorite"
          :loading="favoriteLoading"
          v-if="auth.isLoggedIn && !isOwnStore">
          {{ isFavorited ? '已收藏' : '收藏店铺' }}
        </el-button>
      </div>
    </div>

    <div class="content-section">
      <div class="search-bar">
        <el-input v-model="q" placeholder="搜索商品" @keyup.enter="search" clearable>
          <template #append><el-button type="primary" @click="search">搜索</el-button></template>
        </el-input>
      </div>

      <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
      <el-empty v-else-if="list.length===0" description="暂无商品" />
      <div v-else class="items-grid">
        <div v-for="row in list" :key="row.id" class="item-card" @click="goToItem(row.id)">
          <div class="image-gallery">
            <img :src="row.image || placeholder(row.id)" class="thumb-img" alt="" />
            <div v-if="row.images && row.images.length > 1" class="image-count">
              共{{ row.images.length }}张
            </div>
          </div>
          <div class="item-info">
            <div class="t-title">{{ row.title }}</div>
            <div class="t-desc">{{ row.description }}</div>
            <div class="t-meta">
              <div class="meta-row">
                <span class="t-price">{{ formatCurrency(row.currency) }}{{ row.price }}</span>
                <span v-if="row.soldQuantity !== undefined && row.soldQuantity > 0" class="t-sold">已售：{{ row.soldQuantity }}</span>
              </div>
              <div class="meta-row">
                <el-tag v-if="row.condition" size="small" :type="conditionType(row.condition)">{{ conditionText(row.condition) }}</el-tag>
                <el-tag v-if="row.categoryName" size="small">{{ row.categoryName }}</el-tag>
                <span v-if="row.location" class="location">📍 {{ row.location }}</span>
              </div>
            </div>
            <div
              v-if="!isOwnStore && row.status !== 'sold'"
              class="item-actions"
            >
              <el-button type="primary" size="small" @click.stop="quickBuy(row)">立即购买</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'
import { usePaymentStore } from '../store/payment'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const paymentStore = usePaymentStore()

const storeInfo = ref<any>(null)
const list = ref<any[]>([])
const loading = ref(true)
const q = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const sellerId = ref<number | null>(null)
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// 判断是否是自己的店铺
const isOwnStore = computed(() => {
  if (!auth.isLoggedIn || !auth.user?.userId || !sellerId.value) {
    return false
  }
  return Number(auth.user.userId) === Number(sellerId.value)
})

async function loadStoreInfo() {
  if (!sellerId.value) return
  try {
    const { data } = await http.get(`/api/users/${sellerId.value}`)
    if (data.code === 0) {
      storeInfo.value = data.data
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载店铺信息失败')
  }
}

async function loadFavoriteStatus() {
  if (!auth.isLoggedIn || !sellerId.value || isOwnStore.value) return
  try {
    const { data } = await http.get(`/api/favorites/stores/${sellerId.value}/status`)
    if (data.code === 0) {
      isFavorited.value = data.data.isFavorited || false
    }
  } catch (error: any) {
    // 静默失败，可能未登录
  }
}

async function load() {
  if (!sellerId.value) return
  loading.value = true
  const offset = (page.value - 1) * pageSize.value
  try {
    const { data } = await http.get(`/api/items/seller/${sellerId.value}`, {
      params: { limit: pageSize.value, offset, q: q.value || undefined }
    })
    if (data.code === 0) {
      list.value = data.data?.items || data.data || []
      total.value = data.data?.total || list.value.length
      // 更新店铺信息中的商品数量（仅在第一次加载时）
      if (storeInfo.value && page.value === 1 && !q.value) {
        storeInfo.value.itemCount = total.value
      }
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载商品失败')
  }
  loading.value = false
}

async function toggleFavorite() {
  if (!auth.isLoggedIn || !sellerId.value || isOwnStore.value) {
    ElMessage.warning('请先登录')
    return
  }
  favoriteLoading.value = true
  try {
    const { data } = await http.post(`/api/favorites/stores/${sellerId.value}`)
    if (data.code === 0) {
      isFavorited.value = data.data.isFavorited || false
      ElMessage.success(isFavorited.value ? '已收藏店铺' : '已取消收藏')
    }
  } catch (error: any) {
    if (error?.response?.status === 401) {
      ElMessage.error('请先登录')
    } else {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  } finally {
    favoriteLoading.value = false
  }
}

function search() {
  page.value = 1
  load()
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

function placeholder(id: any) {
  return `https://picsum.photos/seed/store${id}/400/300`
}

async function quickBuy(row: any) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login'))
    return
  }
  try {
    const { data } = await http.post('/api/orders', { itemId: row.id, quantity: 1 })
    if (data.code === 0) {
      const info = data.data
      ElMessage.success('下单成功，请尽快完成支付')
      paymentStore.open({
        id: info.id,
        orderNo: info.orderNo,
        totalAmount: Number(info.totalAmount || row.price),
        currency: info.currency || row.currency || 'CNY',
        expiresAt: info.expiresAt,
        remainingSeconds: info.remainingSeconds,
        sellerId: sellerId.value || row.sellerId
      })
    }
  } catch (error: any) {
    if (error?.response?.status === 401) {
      ElMessage.warning('请先登录')
      window.dispatchEvent(new CustomEvent('show-login'))
    } else {
      ElMessage.error(error?.response?.data?.message || '下单失败')
    }
  }
}

function conditionText(c: string) {
  const map: any = { new: '全新', like_new: '95新', good: '良好', fair: '一般', poor: '较差' }
  return map[c] || c
}

function conditionType(c: string) {
  const map: any = {
    new: 'success',
    like_new: 'success',
    good: 'primary',
    fair: 'warning',
    poor: 'danger'
  }
  return map[c] || 'info'
}

function formatCurrency(currency?: string) {
  if (!currency) return '¥'
  if (currency === 'CNY') return '¥'
  return currency
}

onMounted(() => {
  const id = route.params.sellerId
  if (id) {
    sellerId.value = Number(id)
    loadStoreInfo()
    loadFavoriteStatus()
    load()
  } else {
    ElMessage.error('无效的店铺ID')
    router.push('/')
  }
})
</script>

<style scoped>
.seller-store {
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

.spacer {
  flex: 1;
}

.store-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 16px;
}

.store-avatar {
  flex-shrink: 0;
}

.store-details {
  flex: 1;
}

.store-name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.store-meta {
  color: #666;
  font-size: 14px;
}

.store-actions {
  flex-shrink: 0;
}

.content-section {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
}

.search-bar {
  margin-bottom: 16px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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

.image-gallery {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-count {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.item-info {
  padding: 12px;
}
.item-actions {
  margin-top: 10px;
  text-align: right;
}

.t-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.t-desc {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.t-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.t-price {
  color: #f04848;
  font-weight: 700;
  font-size: 18px;
}

.t-sold {
  color: #52c41a;
  font-size: 12px;
  font-weight: 500;
}

.location {
  color: #666;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
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

