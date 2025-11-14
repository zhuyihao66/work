<template>
  <div class="orders">
    <div class="toolbar">
      <el-button text class="back-btn" @click="goBack">← 返回</el-button>
      <el-tag type="warning" effect="dark">卖家视图</el-tag>
      <el-tabs v-model="status" class="tabs" @tab-click="onTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待发货" name="paid" />
        <el-tab-pane label="已发货" name="shipped" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>
      <div class="search">
        <el-input v-model="q" placeholder="买家/订单号/商品名" @keyup.enter="search">
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
          <div v-if="o.buyer" class="buyer-info">
            <el-avatar :size="24" v-if="o.buyer.avatarUrl" :src="o.buyer.avatarUrl" />
            <el-avatar :size="24" v-else>{{ (o.buyer.displayName || 'U').slice(0, 1).toUpperCase() }}</el-avatar>
            <span class="buyer-name">{{ o.buyer.displayName || '买家' }}</span>
          </div>
          <span class="status">{{ statusText(o.status) }}</span>
        </div>
        <div class="o-body">
          <div class="items">
            <div v-for="it in o.items" :key="it.id" class="thumb">
              <img :src="it.imageUrl || placeholder(it.itemId)" alt="" />
              <div class="t-title line-1">{{ it.title }}</div>
              <div class="t-price">¥{{ it.price }}</div>
            </div>
          </div>
          <div class="summary">
            <div class="amount">实收：<b>¥{{ o.totalAmount }}</b></div>
            <div class="ops">
              <template v-if="o.status==='paid'">
                <el-form :model="shipForms[o.id]" :rules="shipRules" :ref="(el: any) => { if (el) shipFormRefs[o.id] = el }" :inline="true" size="small">
                  <el-form-item prop="provider">
                    <el-input v-model="shipForms[o.id].provider" placeholder="承运商，如：SFExpress" style="width:140px" />
                  </el-form-item>
                  <el-form-item prop="trackingNo">
                    <el-input v-model="shipForms[o.id].trackingNo" placeholder="运单号" style="width:140px" />
                  </el-form-item>
                  <el-button type="primary" @click="doShip(o)">发货</el-button>
                </el-form>
              </template>
              <el-button v-else size="small" @click="toDetail(o)">订单详情</el-button>
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

const router = useRouter()
const status = ref<'all'|'paid'|'shipped'|'completed'|'cancelled'>('all')
const q = ref('')
const orders = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(true)
const shipForms = ref<Record<number, {provider:string; trackingNo:string}>>({})
const shipFormRefs = ref<Record<number, any>>({})
const shipRules = {
  provider: [
    { required: true, message: '请填写承运商', trigger: 'blur' },
    { max: 50, message: '承运商长度不能超过50个字符', trigger: 'blur' }
  ],
  trackingNo: [
    { required: true, message: '请填写运单号', trigger: 'blur' },
    { max: 50, message: '运单号长度不能超过50个字符', trigger: 'blur' }
  ]
}

function placeholder(seed:any){ return `https://picsum.photos/seed/sorder${seed}/120/90` }
function statusText(s:string){
  const map:any = { pending:'待付款', paid:'待发货', shipped:'已发货', completed:'已完成', cancelled:'已取消' }
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
  const { data } = await http.get('/api/orders/seller', { params })
  if (data.code===0){
    orders.value = data.data?.records || data.data || []
    total.value = data.data?.total || orders.value.length
    // 为每个待发货订单初始化发货表单
    orders.value.forEach((o: any) => {
      if (o.status === 'paid' && !shipForms.value[o.id]) {
        shipForms.value[o.id] = { provider: '', trackingNo: '' }
      }
    })
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
function toDetail(o:any){ router.push(`/orders/${o.id}?from=seller`) }
function goBack(){ if (window.history.length>1) router.back(); else router.push('/') }

async function doShip(o:any){
  const formRef = shipFormRefs.value[o.id]
  const shipForm = shipForms.value[o.id]
  
  if (!formRef || !shipForm) return
  
  const ok = await formRef.validate().catch(() => false)
  if (!ok) return
  
  try {
    await http.post(`/api/orders/${o.id}/ship`, { 
      provider: shipForm.provider, 
      trackingNo: shipForm.trackingNo 
    })
    ElMessage.success('发货成功')
    shipForm.provider = ''
    shipForm.trackingNo = ''
    await load()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '发货失败')
  }
}

const handleExternalRefresh = () => load()

onMounted(() => {
  load()
  window.addEventListener('seller-orders:refresh', handleExternalRefresh)
})

onBeforeUnmount(() => {
  window.removeEventListener('seller-orders:refresh', handleExternalRefresh)
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
.o-head .buyer-info{display:flex;align-items:center;gap:6px;margin-left:auto;margin-right:12px}
.o-head .buyer-name{font-size:13px;color:#666}
.o-head .status{color:#ff4d4f;font-weight:600}
.o-body{display:flex;gap:12px;padding:12px}
.items{display:flex;gap:10px;flex-wrap:nowrap;overflow:auto;max-width:820px}
.thumb{width:140px;border:1px solid #f0f0f0;border-radius:6px;overflow:hidden;background:#fff}
.thumb img{width:100%;height:90px;object-fit:cover}
.t-title{padding:6px 8px;font-size:12px}
.t-price{padding:0 8px 8px;color:#f04848;font-weight:700}
.summary{margin-left:auto;display:flex;flex-direction:column;align-items:flex-end;gap:8px;min-width:260px}
.ops{display:flex;gap:8px}
.pager{display:flex;justify-content:center;padding:12px}
.loading-wrap{display:flex;align-items:center;justify-content:center;min-height:200px}
.spinner{width:26px;height:26px;border:3px solid #e5e5e5;border-top-color:#409eff;border-radius:50%;animation:spin 1s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
</style>
