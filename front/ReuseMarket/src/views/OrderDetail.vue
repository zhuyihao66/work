<template>
  <div class="order-detail">
    <div v-if="loadingInfo" class="loading-wrap"><div class="spinner"></div></div>
    <el-empty v-else-if="!info" description="订单不存在或加载失败" />
    <template v-else>
    <el-card shadow="never" class="hero">
      <template #header>
        <div class="hero-head">
          <el-button text class="back-btn" @click="goBack">← 返回</el-button>
          <div class="title">订单详情</div>
          <el-tag :type="statusTag(info.status)" effect="dark">{{ statusText(info.status) }}</el-tag>
        </div>
      </template>
      <div class="hero-main">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="订单号">{{ info.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ fmtTime(info.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">
            <span class="amount">¥{{ info.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="info.buyer" label="买家信息">
            <div class="buyer-detail">
              <el-avatar :size="32" v-if="info.buyer.avatarUrl" :src="info.buyer.avatarUrl" />
              <el-avatar :size="32" v-else>{{ (info.buyer.displayName || 'U').slice(0, 1).toUpperCase() }}</el-avatar>
              <span style="margin-left:8px">{{ info.buyer.displayName || '买家' }}</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
        <div class="steps-wrap">
          <el-steps :active="stepIndex(info.status)" align-center finish-status="success">
            <el-step title="下单" />
            <el-step title="已付款" />
            <el-step title="已发货" />
            <el-step title="已完成" />
          </el-steps>
        </div>
        <div class="actions">
          <el-button v-if="info.status==='pending'" @click="pay" type="primary">模拟支付</el-button>
          <el-button v-if="info.status==='pending'" @click="cancel" type="danger" plain>取消订单</el-button>
          <el-button v-if="info.status==='shipped' && !sellerView" @click="confirm" type="success">确认收货</el-button>
        </div>
      </div>
    </el-card>

    <div class="layout">
      <el-card class="left" shadow="never">
        <template #header><b>商品清单</b></template>
        <div class="item-list">
          <div v-for="it in info.items || []" :key="it.id" class="row">
            <img 
              :src="it.imageUrl || placeholder(it.itemId)" 
              @click="goToItem(it.itemId)"
              style="cursor: pointer;"
              class="item-img" />
            <div class="col">
              <div class="r-title line-1">{{ it.title }}</div>
              <div class="r-desc line-2">{{ it.description }}</div>
              <div class="r-meta"><span class="price">¥{{ it.price }}</span><span class="qty">× {{ it.quantity }}</span></div>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="right" shadow="never">
        <template #header><b>物流信息</b></template>
        <el-skeleton :loading="loadingShip" animated :rows="3">
          <el-timeline>
            <el-timeline-item v-for="s in shipments" :key="s.id" :timestamp="fmtTime(s.createdAt)" :type="statusColor(s.status)">
              <div class="ship-row">
                <el-tag size="small">{{ s.status }}</el-tag>
                <span>承运商：{{ s.shippingProvider }}，运单号：{{ s.trackingNo }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-skeleton>
      </el-card>
    </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const info = ref<any>(null)
const loadingInfo = ref(true)
const shipments = ref<any[]>([])
const loadingShip = ref(true)
const sellerView = computed(() => route.query.from === 'seller')
// 发货 UI 已移除（仅在我的出售页面可发货）

async function load(){
  const id = route.params.id
  const fromSeller = sellerView.value
  let orderLoaded = false
  
  // 根据来源决定使用哪个接口
  try {
    const url = fromSeller ? `/api/orders/seller/${id}` : `/api/orders/${id}`
    const d1 = await http.get(url)
    if (d1.data.code === 0) {
      info.value = d1.data.data
      orderLoaded = true
    } else {
      ElMessage.error(d1.data.message || '订单不存在')
      info.value = null
    }
  } catch (error: any) {
    // 如果指定接口失败，尝试另一个接口（兼容性处理）
    if (!fromSeller && (error?.response?.status === 400 || error?.response?.status === 404)) {
      try {
        const d2 = await http.get(`/api/orders/seller/${id}`)
        if (d2.data.code === 0) {
          info.value = d2.data.data
          orderLoaded = true
        } else {
          ElMessage.error(d2.data.message || '订单不存在')
          info.value = null
        }
      } catch (error2: any) {
        ElMessage.error(error2?.response?.data?.message || '订单不存在')
        info.value = null
      }
    } else {
      ElMessage.error(error?.response?.data?.message || '加载订单失败')
      info.value = null
    }
  }
  
  // 加载物流信息（如果订单加载成功）
  if (orderLoaded) {
    try {
      const d2 = await http.get(`/api/orders/${id}/shipments`)
      if (d2.data.code === 0) shipments.value = d2.data.data || []
    } catch (error: any) {
      // 物流信息加载失败不影响主流程
      shipments.value = []
    }
  }
  
  loadingShip.value = false
  loadingInfo.value = false
}

async function cancel(){
  await ElMessageBox.confirm('确定取消该订单？','提示')
  const id = route.params.id
  const { data } = await http.post(`/api/orders/${id}/cancel`)
  if (data.code===0){ ElMessage.success('已取消'); await load() } else ElMessage.error(data.message)
}

async function pay(){
  const id = route.params.id
  const { data } = await http.post(`/api/orders/${id}/pay`)
  if (data.code===0){ ElMessage.success('已支付'); await load() } else ElMessage.error(data.message)
}

// 发货逻辑仅在我的出售页面触发

async function confirm(){
  if (sellerView.value) {
    ElMessage.warning('卖家无需确认收货')
    return
  }
  const id = route.params.id
  const { data } = await http.post(`/api/orders/${id}/confirm`)
  if (data.code===0){ ElMessage.success('已完成'); await load() } else ElMessage.error(data.message)
}

function goToItem(itemId: number){ router.push(`/item/${itemId}`) }
function placeholder(seed:any){ return `https://picsum.photos/seed/order${seed}/120/90` }

function fmtTime(t:any){
  return t ? String(t).replace('T',' ') : ''
}
function statusColor(status:string){
  switch(status){
    case 'delivered': return 'success'
    case 'in_transit': return 'primary'
    case 'shipped': return 'primary'
    case 'returned': return 'warning'
    case 'lost': return 'danger'
    default: return ''
  }
}

function statusText(s:string){
  const map:any = { pending:'待付款', paid:'已付款', shipped:'已发货', completed:'已完成', cancelled:'已取消' }
  return map[s] || s
}
function statusTag(s:string){
  switch(s){
    case 'pending': return 'warning'
    case 'paid': return 'success'
    case 'shipped': return 'primary'
    case 'completed': return 'success'
    case 'cancelled': return 'info'
    default: return ''
  }
}
function stepIndex(s:string){
  return s==='pending'?1 : s==='paid'?2 : s==='shipped'?3 : s==='completed'?4 : 1
}

onMounted(load)

function goBack(){ if (window.history.length>1) router.back(); else router.push('/') }
</script>

<style scoped>
.order-detail{padding:16px}
.item-img:hover{opacity:0.8}
.hero .title{font-size:18px;font-weight:700}
.hero-head{display:flex;align-items:center;justify-content:space-between}
.back-btn{color:#666}
.hero-main{display:flex;flex-direction:column;gap:12px}
.steps-wrap{padding:6px 0}
.amount{color:#f04848;font-weight:800}
.actions{margin:8px 0;display:flex;gap:8px}
.hint{color:#999}
.buyer-detail{display:flex;align-items:center}
.layout{display:grid;grid-template-columns:1fr;gap:12px;margin-top:12px}
.ship-row{display:flex;align-items:center;gap:8px}
.item-list .row{display:flex;gap:10px;border-bottom:1px solid #f5f5f5;padding:10px 0}
.item-list img{width:96px;height:72px;object-fit:cover;border-radius:6px}
.item-list .col{flex:1}
.r-title{font-size:14px}
.r-desc{color:#888;font-size:12px;margin:2px 0}
.r-meta{display:flex;gap:12px;align-items:center}
.r-meta .price{color:#f04848;font-weight:700}
.r-meta .qty{color:#666;font-size:12px}
.loading-wrap{display:flex;align-items:center;justify-content:center;min-height:240px}
.spinner{width:26px;height:26px;border:3px solid #e5e5e5;border-top-color:#409eff;border-radius:50%;animation:spin 1s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
</style>


