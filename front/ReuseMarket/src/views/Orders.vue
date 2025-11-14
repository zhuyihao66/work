<template>
  <div class="orders">
    <div class="toolbar">
      <el-button text class="back-btn" @click="goBack">← 返回</el-button>
      <el-tabs v-model="status" class="tabs" @tab-click="onTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待付款" name="pending" />
        <el-tab-pane label="已付款" name="paid" />
        <el-tab-pane label="已发货" name="shipped" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>
      <div class="search">
        <el-input v-model="q" placeholder="商品标题/订单号/店铺名" @keyup.enter="search">
          <template #append>
            <el-button type="primary" @click="search">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
    <el-empty v-else-if="orders.length===0" description="暂无订单" />
    <div v-else>
      <div v-for="o in orders" :key="o.id" class="order-card">
        <div class="o-head">
          <span class="date">下单时间：{{ formatDate(o.createdAt) }}</span>
          <span class="no">订单号：{{ o.orderNo }}</span>
          <div v-if="o.seller" class="seller-info">
            <el-avatar :size="24" v-if="o.seller.avatarUrl" :src="o.seller.avatarUrl" />
            <el-avatar :size="24" v-else>{{ (o.seller.displayName || 'S').slice(0, 1).toUpperCase() }}</el-avatar>
            <span class="seller-name">{{ o.seller.displayName || '卖家' }}</span>
          </div>
          <span class="status">{{ statusText(o.status) }}</span>
        </div>
        <div class="o-body">
          <div class="items">
            <div v-for="it in o.items" :key="it.id" class="thumb">
              <img 
                :src="it.imageUrl || placeholder(it.itemId)" 
                alt="" 
                class="thumb-img"
                @click="goToItem(it.itemId)"
                style="cursor: pointer;" />
              <div class="t-title line-1">{{ it.title }}</div>
              <div class="t-desc line-2">{{ it.description }}</div>
              <div class="t-row"><span class="t-price">¥{{ it.price }}</span><span class="t-qty">× {{ it.quantity }}</span></div>
            </div>
          </div>
          <div class="summary">
            <div class="amount">{{ o.status==='pending' ? '应付' : '实付' }}：<b>¥{{ o.totalAmount }}</b></div>
            <div v-if="o.status==='pending'" class="countdown-row" :class="{ danger: remainingSeconds(o) <= 10 }">
              剩余支付时间：{{ formatCountdown(remainingSeconds(o)) }}
            </div>
            <div class="ops">
              <el-button
                v-if="o.status==='pending'"
                type="primary"
                size="small"
                :disabled="remainingSeconds(o) === 0"
                @click="openPayment(o)"
              >模拟支付</el-button>
              <el-button v-if="o.status==='pending'" size="small" @click="cancel(o)" plain type="danger">取消订单</el-button>
              <el-button v-if="o.status==='paid'" size="small" @click="toDetail(o)">查看物流</el-button>
              <el-button size="small" @click="toDetail(o)">订单详情</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="pager">
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import http from '../api/http'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { usePaymentStore } from '../store/payment'

const router = useRouter()
const paymentStore = usePaymentStore()
const status = ref<'all'|'pending'|'paid'|'shipped'|'completed'|'cancelled'>('all')
const q = ref('')
const orders = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(true)
const nowTick = ref(Date.now())
let tickTimer: number | null = null

function placeholder(seed:any){ return `https://picsum.photos/seed/order${seed}/120/90` }
function statusText(s:string){
  const map:any = { pending:'待付款', paid:'已付款', shipped:'已发货', completed:'已完成', cancelled:'已取消' }
  return map[s] || s
}
function formatDate(iso:string){
  if(!iso) return ''
  return iso.replace('T',' ').slice(0,19)
}

async function load(){
  loading.value = true
  const offset = (page.value-1)*pageSize.value
  const params:any = { limit: pageSize.value, offset, q: q.value || undefined, status: status.value==='all'? undefined: status.value }
  const { data } = await http.get('/api/orders', { params })
  if (data.code===0){
    orders.value = data.data?.records || data.data || []
    total.value = data.data?.total || orders.value.length
  }
  loading.value = false
}

function onTab(pane?: any){
  if (pane && pane.paneName) status.value = pane.paneName as any
  else if (pane && pane.props?.name) status.value = pane.props.name as any
  page.value = 1; load()
}
function search(){ page.value = 1; load() }
function onPage(p:number){ page.value = p; load() }
function toDetail(o:any){ router.push(`/orders/${o.id}`) }
function goToItem(itemId: number){ router.push(`/item/${itemId}`) }
function goBack(){ if (window.history.length>1) router.back(); else router.push('/') }

async function cancel(o:any){
  const { data } = await http.post(`/api/orders/${o.id}/cancel`)
  if (data.code===0) load()
}

function remainingSeconds(o: any) {
  if (o.status !== 'pending' || !o.expiresAt) return 0
  const diff = Math.floor((new Date(o.expiresAt).getTime() - nowTick.value) / 1000)
  return Math.max(0, diff)
}

function formatCountdown(sec: number) {
  const mm = String(Math.floor(sec / 60)).padStart(2, '0')
  const ss = String(sec % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

function openPayment(o: any) {
  const remain = remainingSeconds(o)
  if (remain === 0) {
    ElMessage.warning('订单已超时，请重新下单')
    load()
    return
  }
  paymentStore.open({
    id: o.id,
    orderNo: o.orderNo,
    totalAmount: Number(o.totalAmount),
    currency: o.currency || 'CNY',
    expiresAt: o.expiresAt,
    remainingSeconds: remain,
    sellerId: o.seller?.id
  })
}

const handleExternalRefresh = () => load()

onMounted(() => {
  load()
  tickTimer = window.setInterval(() => {
    nowTick.value = Date.now()
  }, 1000)
  window.addEventListener('orders:refresh', handleExternalRefresh)
})

onBeforeUnmount(() => {
  if (tickTimer) {
    clearInterval(tickTimer)
    tickTimer = null
  }
  window.removeEventListener('orders:refresh', handleExternalRefresh)
})
</script>

<style scoped>
.orders{max-width:1200px;margin:0 auto;padding:12px 16px}
.toolbar{display:flex;align-items:center;gap:12px}
.back-btn{color:#666}
.toolbar .tabs :deep(.el-tabs__header){margin-bottom:0}
.toolbar .search{flex:1}
.order-card{border:1px solid #eee;border-radius:8px;background:#fff;margin-top:12px}
.o-head{display:flex;align-items:center;gap:12px;padding:10px 12px;border-bottom:1px solid #f5f5f5;background:#fafafa}
.o-head .seller-info{display:flex;align-items:center;gap:6px}
.o-head .seller-name{font-size:14px;color:#333}
.o-head .status{margin-left:auto;color:#ff4d4f;font-weight:600}
.o-body{display:flex;gap:12px;padding:12px}
.items{display:flex;gap:10px;flex-wrap:nowrap;overflow:auto;max-width:820px}
.thumb{width:140px;border:1px solid #f0f0f0;border-radius:6px;overflow:hidden;background:#fff}
.thumb img{width:100%;height:90px;object-fit:cover}
.thumb-img:hover{opacity:0.8}
.t-title{padding:6px 8px;font-size:12px}
.t-desc{padding:0 8px;color:#888;font-size:12px}
.t-row{display:flex;justify-content:space-between;align-items:center;padding:0 8px 8px}
.t-price{color:#f04848;font-weight:700}
.t-qty{color:#666;font-size:12px}
.summary{margin-left:auto;display:flex;flex-direction:column;align-items:flex-end;gap:8px;min-width:220px}
.amount{font-size:14px}
.countdown-row{font-size:12px;color:#666}
.countdown-row.danger{color:#d4380d}
.pager{display:flex;justify-content:center;padding:12px}

.loading-wrap{display:flex;align-items:center;justify-content:center;min-height:200px}
.spinner{width:26px;height:26px;border:3px solid #e5e5e5;border-top-color:#409eff;border-radius:50%;animation:spin 1s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}

</style>


