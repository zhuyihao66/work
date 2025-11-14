<template>
  <div class="toolbar">
    <el-button text @click="goBack">← 返回</el-button>
    <div class="spacer"></div>
    <el-button text @click="$router.push('/')">首页</el-button>
    <el-button text @click="$router.push('/orders')">我的订单</el-button>
  </div>
  <div class="detail" v-if="item">
    <div class="gallery">
      <div class="main" 
           @mouseenter="showMagnifier = true"
           @mouseleave="showMagnifier = false"
           @mousemove="handleMouseMove"
           ref="mainImgContainer">
        <img :src="currentImg" alt="" ref="mainImg" />
        <div class="magnifier" v-if="showMagnifier && currentImg" 
             :style="{ left: magnifierPos.x + 'px', top: magnifierPos.y + 'px' }">
          <img :src="currentImg" alt="" 
               :style="{ 
                 width: magnifierPos.imgWidth + 'px',
                 height: magnifierPos.imgHeight + 'px',
                 objectFit: 'contain',
                 transform: `translate(${magnifierPos.imgX}px, ${magnifierPos.imgY}px)`,
                 transformOrigin: 'top left'
               }" />
        </div>
      </div>
      <div class="thumbs" v-if="item.images && item.images.length">
        <img v-for="(img,idx) in item.images" :key="idx" :src="img" :class="{active: currentImg===img}"
             @click="currentImg = img" />
      </div>
    </div>
    <div class="info">
      <h2>{{ item.title }}</h2>
      <div class="price">¥{{ item.price }}</div>
      <div class="meta">
        <div>成色：<el-tag size="small" :type="condType(item.condition)">{{ condText(item.condition) }}</el-tag></div>
        <div>位置：{{ item.location || '—' }}</div>
      </div>
      <div class="desc" v-if="item.description">{{ item.description }}</div>
      <div class="seller">
        <el-avatar :size="32" v-if="item.seller?.avatarUrl" :src="item.seller.avatarUrl" style="margin-right:8px" />
        <el-avatar :size="32" v-else style="margin-right:8px">{{ (item.seller?.displayName||'U').slice(0,1).toUpperCase() }}</el-avatar>
        <span>{{ item.seller?.displayName }}</span>
        <el-button 
          v-if="item.seller?.id && !isOwnItem" 
          text 
          type="primary" 
          size="small"
          @click="$router.push(`/store/${item.seller.id}`)"
          style="margin-left: 12px">
          进入店铺
        </el-button>
      </div>
      <el-button 
        v-if="isOwnItem" 
        type="info" 
        disabled
        size="large"
      >这是您上架的商品</el-button>
      <el-button 
        v-else 
        type="primary" 
        @click="openAddr"
        size="large"
      >立即购买</el-button>
      <div class="pager">
        <el-button text :disabled="!nav.prevId" @click="goItem(nav.prevId)">上一件</el-button>
        <el-button text :disabled="!nav.nextId" @click="goItem(nav.nextId)">下一件</el-button>
      </div>
    </div>
    <div class="full">
      <h3 class="reviews-title">用户评价</h3>
    </div>
    <!-- 评论表单和评论列表并排显示 -->
    <div class="full reviews-layout">
      <!-- 评论表单 - 左侧，宽度500px -->
      <div v-if="auth.isLoggedIn && !isOwnItem" class="review-form-container">
        <el-card shadow="never">
          <h4>我要评价</h4>
          <el-form :model="reviewForm" label-width="80px">
            <el-form-item label="选择订单" v-if="completedOrders.length > 0">
              <el-select v-model="reviewForm.orderId" placeholder="请选择要评价的订单" style="width: 100%">
                <el-option 
                  v-for="order in completedOrders" 
                  :key="order.id" 
                  :label="`订单号: ${order.orderNo} - ¥${order.totalAmount}`"
                  :value="order.id" />
              </el-select>
            </el-form-item>
            <el-form-item v-else>
              <el-alert type="info" :closable="false">
                <template #default>
                  <div>暂无可评价的订单。只有已完成的订单才能评价。</div>
                </template>
              </el-alert>
            </el-form-item>
            <el-form-item label="评分" required>
              <el-rate v-model="reviewForm.rating" :max="5" show-text />
            </el-form-item>
            <el-form-item label="评论内容">
              <el-input 
                v-model="reviewForm.comment" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入您的评价（可选）"
                maxlength="500"
                show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submittingReview" @click="submitReview">提交评价</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      <!-- 评论列表 - 右侧，占据剩余空间 -->
      <div class="reviews-section">
        <div class="reviews-container">
          <el-empty v-if="!item.reviews || item.reviews.length===0" description="暂无评价" />
          <el-timeline v-else>
            <el-timeline-item v-for="r in item.reviews" :key="r.id" :timestamp="fmtTime(r.createdAt)">
              <div class="review-item">
                <div class="reviewer-info">
                  <el-avatar :size="32" v-if="r.reviewer?.avatarUrl" :src="r.reviewer.avatarUrl" />
                  <el-avatar :size="32" v-else>{{ (r.reviewer?.displayName||'U').slice(0,1).toUpperCase() }}</el-avatar>
                  <span class="reviewer-name">{{ r.reviewer?.displayName || '匿名用户' }}</span>
                </div>
                <el-rate :model-value="r.rating" disabled />
                <div class="comment">{{ r.comment || '暂无评论内容' }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
  </div>
  <el-dialog v-model="addrShow" title="选择收货地址" width="520px">
    <el-radio-group v-model="selectedAddrId">
      <div v-for="a in addresses" :key="a.id" class="addr-item">
        <el-radio :label="a.id">
          {{ a.receiverName }} {{ a.phone }} ｜ {{ a.country }} {{ a.province }} {{ a.city }} {{ a.district }} {{ a.addressLine }}
        </el-radio>
      </div>
    </el-radio-group>
    <template #footer>
      <el-button @click="addrShow=false">取消</el-button>
      <el-button @click="openNewAddr">新增地址</el-button>
      <el-button type="primary" :disabled="!selectedAddrId" @click="addrShow=false; buy()">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="newAddrShow" title="新增收货地址" width="560px">
    <el-form ref="addrRef" :model="newAddr" :rules="addrRules" label-width="90px" class="addr-form">
      <div class="addr-grid">
        <el-form-item label="收件人" prop="receiverName"><el-input v-model="newAddr.receiverName" /></el-form-item>
        <el-form-item label="电话" prop="phone"><el-input v-model="newAddr.phone" /></el-form-item>
        <el-form-item label="国家/地区"><el-input v-model="newAddr.country" /></el-form-item>
        <el-form-item label="省"><el-input v-model="newAddr.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="newAddr.city" /></el-form-item>
        <el-form-item label="区/县"><el-input v-model="newAddr.district" /></el-form-item>
        <el-form-item label="详细地址" class="full" prop="addressLine"><el-input v-model="newAddr.addressLine" /></el-form-item>
        <el-form-item label="邮编"><el-input v-model="newAddr.postalCode" /></el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="newAddrShow=false">取消</el-button>
      <el-button type="primary" :loading="savingAddr" @click="createAddr">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteUpdate } from 'vue-router'
import http from '../api/http'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { usePaymentStore } from '../store/payment'

const route = useRoute()
const router = useRouter()
const item = ref<any>(null)
const currentImg = ref<string>('')
const nav = ref<{prevId?: number|null; nextId?: number|null}>({})
const addrShow = ref(false)
const addresses = ref<any[]>([])
const selectedAddrId = ref<number|null>(null)
const newAddrShow = ref(false)
const newAddr = ref<any>({ receiverName:'', phone:'', country:'CN', province:'', city:'', district:'', addressLine:'', postalCode:'' })
const addrRules = {
  receiverName: [{ required: true, message: '请填写收件人', trigger: 'blur' }],
  phone: [{ required: true, message: '请填写电话', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  addressLine: [{ required: true, message: '请填写详细地址', trigger: 'blur' }]
}
const addrRef = ref()
const savingAddr = ref(false)
const auth = useAuthStore()
const paymentStore = usePaymentStore()

// 评论相关
const reviewForm = ref({ orderId: null as number | null, rating: 0, comment: '' })
const submittingReview = ref(false)
const completedOrders = ref<any[]>([])
const canReview = computed(() => {
  return auth.isLoggedIn && !isOwnItem.value && completedOrders.value.length > 0
})

// 放大镜相关
const showMagnifier = ref(false)
const mainImgContainer = ref<HTMLElement>()
const mainImg = ref<HTMLImageElement>()
const magnifierSize = 200 // 放大镜窗口大小
const zoom = 2.5 // 放大倍数
const magnifierPos = ref({
  x: 0,
  y: 0,
  imgX: 0,
  imgY: 0,
  imgWidth: 0,
  imgHeight: 0
})

// 判断是否是本人上架的商品
const isOwnItem = computed(() => {
  try {
    if (!auth.isLoggedIn || !auth.user?.userId) {
      return false
    }
    if (!item.value || !item.value.seller || !item.value.seller.id) {
      return false
    }
    // 确保类型匹配（后端可能返回 Long 类型，前端 userId 是 number）
    const sellerId = Number(item.value.seller.id)
    const userId = Number(auth.user.userId)
    const result = sellerId === userId && sellerId > 0 && userId > 0
    return result
  } catch (e) {
    console.error('Error checking isOwnItem:', e)
    return false
  }
})

async function loadById(id:any){
  const { data } = await http.get(`/api/items/${id}`)
  if (data.code === 0) {
    item.value = data.data
    currentImg.value = item.value.images?.[0] || ''
    // 记录浏览历史（如果已登录）
    if (auth.isLoggedIn) {
      try {
        await http.post(`/api/favorites/history/${id}`)
      } catch (e) {
        // 静默失败，不影响主流程
      }
      // 加载用户已完成的订单（用于评价）
      await loadCompletedOrders(id)
    }
  }
  const n = await http.get(`/api/items/nav`, { params: { currentId: id } })
  if (n.data.code === 0) nav.value = n.data.data || {}
}

async function loadCompletedOrders(itemId: any) {
  try {
    // 获取用户的所有订单
    const { data } = await http.get('/api/orders')
    if (data.code === 0) {
      const orders = data.data?.records || data.data || []
      const itemIdNum = Number(itemId)
      // 筛选出已完成的订单，且订单中包含该商品
      const completed = orders.filter((o: any) => {
        if (o.status !== 'completed') return false
        // 检查订单是否包含该商品
        if (o.items && Array.isArray(o.items)) {
          return o.items.some((oi: any) => {
            const oiItemId = Number(oi.itemId)
            return oiItemId === itemIdNum
          })
        }
        return false
      })
      completedOrders.value = completed
      // 如果有订单，默认选择第一个
      if (completed.length > 0 && !reviewForm.value.orderId) {
        reviewForm.value.orderId = completed[0].id
      }
      // 调试信息
      console.log('加载完成订单:', {
        itemId: itemIdNum,
        totalOrders: orders.length,
        completedOrders: completed.length,
        orders: completed
      })
    }
  } catch (e: any) {
    console.error('加载已完成订单失败:', e)
    completedOrders.value = []
  }
}

async function submitReview() {
  if (completedOrders.value.length === 0) {
    ElMessage.warning('暂无可评价的订单')
    return
  }
  if (!reviewForm.value.orderId || !reviewForm.value.rating) {
    ElMessage.warning('请选择订单并评分')
    return
  }
  submittingReview.value = true
  try {
    const { data } = await http.post('/api/reviews', {
      orderId: reviewForm.value.orderId,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.comment || null
    })
    if (data.code === 0) {
      ElMessage.success('评价提交成功')
      // 重置表单
      reviewForm.value = { orderId: null, rating: 0, comment: '' }
      // 重新加载商品详情以刷新评论列表
      await loadById(item.value.id)
      // 重新加载订单列表
      await loadCompletedOrders(item.value.id)
    } else {
      ElMessage.error(data.message || '评价提交失败')
    }
  } catch (e: any) {
    if (e?.response?.status === 401) {
      ElMessage.error('请先登录')
    } else {
      ElMessage.error(e?.response?.data?.message || '评价提交失败')
    }
  } finally {
    submittingReview.value = false
  }
}

async function buy(){
  try{
    const { data } = await http.post('/api/orders', { itemId: item.value.id, quantity: 1, note: selectedAddrId.value ? `addressId=${selectedAddrId.value}` : undefined })
    if (data.code !== 0) throw new Error(data.message)
    const info = data.data
    ElMessage.success('下单成功，请尽快完成支付')
    paymentStore.open({
      id: info.id,
      orderNo: info.orderNo,
      totalAmount: Number(info.totalAmount || item.value.price),
      currency: info.currency || 'CNY',
      expiresAt: info.expiresAt,
      remainingSeconds: info.remainingSeconds,
      sellerId: item.value.seller?.id
    })
  }catch(e:any){
    if (e?.response?.status === 401) ElMessage.error('请先登录')
    else ElMessage.error(e?.response?.data?.message || e.message || '下单失败')
  }
}

function fmtTime(t:any){ return t ? String(t).replace('T',' ') : '' }

function condText(v:string){
  return ({ new:'全新', like_new:'95新', good:'良好', fair:'一般', poor:'较差' } as any)[v] || (v || '—')
}
function condType(v:string){
  const map:any = { new:'success', like_new:'success', good:'primary', fair:'warning', poor:'danger' }
  return map[v] || 'info'
}

function goBack(){
  if (window.history.length > 1) router.back()
  else router.push('/')
}

function goItem(id:any){ if (id) router.push(`/item/${id}`) }

function handleMouseMove(e: MouseEvent) {
  if (!mainImgContainer.value || !mainImg.value || !showMagnifier.value) return
  
  const img = mainImg.value
  const imgRect = img.getBoundingClientRect()
  
  // 鼠标相对于图片的位置
  const x = e.clientX - imgRect.left
  const y = e.clientY - imgRect.top
  
  // 图片实际显示尺寸
  const imgWidth = imgRect.width
  const imgHeight = imgRect.height
  
  // 计算图片上的相对位置，限制在图片范围内
  const imgX = Math.max(0, Math.min(imgWidth, x))
  const imgY = Math.max(0, Math.min(imgHeight, y))
  
  // 放大镜窗口位置（跟随鼠标，偏移一点避免遮挡）
  const offsetX = 20
  const offsetY = 20
  
  // 计算放大镜窗口位置，确保不超出视口
  let magnifierX = e.clientX + offsetX
  let magnifierY = e.clientY + offsetY
  
  // 如果放大镜会超出右边界，则显示在鼠标左侧
  if (magnifierX + magnifierSize > window.innerWidth) {
    magnifierX = e.clientX - magnifierSize - offsetX
  }
  
  // 如果放大镜会超出下边界，则显示在鼠标上方
  if (magnifierY + magnifierSize > window.innerHeight) {
    magnifierY = e.clientY - magnifierSize - offsetY
  }
  
  // 计算放大图片的偏移量，使鼠标位置对应放大镜中心
  // 鼠标在图片上的位置（imgX, imgY）在放大后的图片上对应（imgX * zoom, imgY * zoom）
  // 要让这个位置显示在放大镜中心（magnifierSize/2），需要调整偏移量
  const translateX = magnifierSize / 2 - imgX * zoom
  const translateY = magnifierSize / 2 - imgY * zoom
  
  magnifierPos.value = {
    x: magnifierX,
    y: magnifierY,
    imgX: translateX,
    imgY: translateY,
    imgWidth: imgWidth * zoom,
    imgHeight: imgHeight * zoom
  }
}

async function openAddr(){
  try{
    const { data } = await http.get('/api/me/addresses')
    if (data.code===0) {
      addresses.value = data.data
      selectedAddrId.value = data.data?.find((a:any)=>a.isDefault===1)?.id || (data.data[0]?.id ?? null)
      addrShow.value = true
    }
  }catch{ addrShow.value = true }
}

function openNewAddr(){ newAddrShow.value = true }
async function createAddr(){
  const ok = await addrRef.value.validate().catch(()=>false)
  if (!ok) return
  savingAddr.value = true
  try{
    const { data } = await http.post('/api/me/addresses', newAddr.value)
    if (data.code===0){
      newAddrShow.value = false
      await openAddr()
      selectedAddrId.value = data.data
      ElMessage.success('已新增地址')
    } else ElMessage.error(data.message)
  }catch(e:any){ ElMessage.error(e?.response?.data?.message || '新增失败') }
  finally{ savingAddr.value = false }
}

onMounted(() => loadById(route.params.id))
onBeforeRouteUpdate((to) => {
  item.value = null
  currentImg.value = ''
  nav.value = {}
  loadById(to.params.id)
})
</script>

<style scoped>
.toolbar{display:flex;align-items:center;padding:8px 16px;border-bottom:1px solid #eee;background:#fff}
.spacer{flex:1}
.detail{display:grid;grid-template-columns:1fr 1fr;gap:32px;padding:16px}
.gallery{display:flex;flex-direction:column;gap:8px;border:2px solid #f04848;border-radius:8px;padding:8px;background:#fff}
.gallery .main{position:relative;cursor:crosshair}
.gallery .main img{width:100%;max-height:360px;object-fit:contain;border:none;border-radius:4px;background:#fff}
.gallery .magnifier{
  position:fixed;
  width:200px;
  height:200px;
  border:2px solid #409eff;
  border-radius:8px;
  overflow:hidden;
  background:#fff;
  box-shadow:0 4px 12px rgba(0,0,0,0.15);
  z-index:1000;
  pointer-events:none;
}
.gallery .magnifier img{
  display:block;
}
.gallery .thumbs{
  display:flex;
  gap:8px;
  overflow-x:auto;
  border-top:1px solid #e0e0e0;
  padding-top:8px;
  margin-top:8px;
}
.gallery .thumbs img{width:72px;height:72px;object-fit:cover;border:2px solid transparent;border-radius:6px;cursor:pointer}
.gallery .thumbs img.active{border-color:#409eff}
.price{color:#f04848;font-weight:700;margin:12px 0}
.meta{display:flex;gap:16px;color:#666;margin:8px 0}
.desc{white-space:pre-wrap;color:#333;margin:8px 0}
.seller{display:flex;align-items:center;gap:6px;margin:12px 0}
.pager{margin-top:24px}
.full{grid-column:1 / span 2;margin-top:8px}
.comment{
  margin-top: 8px;
  color: #666;
  line-height: 1.6;
  font-size: 14px;
}
.addr-item{padding:6px 0}

/* 用户评价标题 - 独立显示在最左侧 */
.reviews-title {
  text-align: left;
  margin: 16px 0;
  padding: 0 16px 12px 16px; /* 与详情页的padding保持一致，底部增加padding */
  border-bottom: 2px solid #f04848; /* 红色分割线 */
}

/* 评论布局容器 - 使用flex布局让表单和列表并排 */
.reviews-layout {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  gap: 24px; /* 两个容器之间的间距 */
  padding: 0 16px; /* 与详情页的padding保持一致 */
}

/* 当评论表单不显示时，评论列表占据全部宽度 */
/* 评论表单容器 - 左侧，宽度固定500px */
.review-form-container {
  flex: 0 0 500px; /* 固定宽度500px，不伸缩 */
  max-width: 500px;
}

.review-form-container h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  text-align: left;
}

/* 评论区样式 - 右侧，占据剩余空间 */
.reviews-section {
  flex: 1; /* 占据剩余空间 */
  min-width: 0; /* 允许收缩 */
  padding: 0 20px 0 8px; /* 右侧留白20px，左侧留白8px（往左调整） */
}

.reviews-container {
  border: 1px solid #e0e0e0; /* 添加边框 */
  border-radius: 8px; /* 圆角 */
  padding: 16px; /* 内边距 */
  background: #fff; /* 背景色 */
}

/* 当没有评论表单时，评论列表占据全部宽度 */
.reviews-layout:not(:has(.review-form-container)) .reviews-section {
  max-width: 1000px; /* 限制最大宽度 */
  margin: 0 auto; /* 居中显示 */
}



.review-item {
  border: 1px solid #f0f0f0; /* 卡片边框 */
  border-radius: 8px; /* 圆角 */
  padding: 16px; /* 内边距 */
  margin-bottom: 12px; /* 卡片之间的间距 */
  margin-left: -20px; /* 往左调整20px */
  background: #fff; /* 背景色 */
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08); /* 轻微阴影效果 */
  transition: box-shadow 0.2s ease; /* 过渡效果 */
}

.review-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12); /* 悬停时增强阴影 */
}

.review-item:last-child {
  margin-bottom: 0; /* 最后一条评论不需要底部间距 */
}

/* 隐藏时间线组件的标记点 */
.reviews-container :deep(.el-timeline-item__node) {
  display: none;
}

/* 隐藏时间线连接线 */
.reviews-container :deep(.el-timeline-item__wrapper) {
  padding-left: 0;
}

.reviews-container :deep(.el-timeline-item__tail) {
  display: none;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px; /* 增加底部间距 */
}

.reviewer-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}
</style>


