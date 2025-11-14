<template>
  <div class="tao-header">
    <div class="container">
      <div class="logo">二手交易平台</div>
      <div class="search-wrap">
        <el-input v-model="q" placeholder="搜宝贝/分类/关键词" @keyup.enter="search" class="search-input">
          <template #append>
            <el-button type="primary" class="search-btn" @click="search">搜索</el-button>
          </template>
        </el-input>
        <div class="hot-links">
          <el-link v-for="k in hotKeys" :key="k" :class="{ 'active': selectedHotKey === k }" @click="clickHotKey(k)">{{ k }}</el-link>
        </div>
      </div>
      
    </div>
  </div>
  <div class="promo">
    <div class="container promo-layout">
      <div class="cats">
        <div class="cats-title">全部分类</div>
        <div v-if="loadingCats" class="spinner-wrap"><div class="spinner"></div></div>
        <div v-else class="cat-grid">
          <div
            v-for="c in topLevelCats"
            :key="c.id"
            class="cat-tag"
            :class="{ 'active': selectedCategoryId === c.id }"
            @click="goTag(c)"
          >
            <span class="tag-emoji">{{ emojiFor(lastName(c.label)) }}</span>
            <span class="tag-name">{{ lastName(c.label) }}</span>
          </div>
        </div>
      </div>
      <div class="carousel">
        <el-carousel height="280px" indicator-position="outside" :interval="4000">
          <el-carousel-item v-for="(b,idx) in banners" :key="idx">
            <img :src="b" class="banner" />
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="login-card">
        <div class="user-row">
          <el-avatar v-if="auth.isLoggedIn && auth.user?.avatarUrl" :size="44" :src="auth.user.avatarUrl" />
          <el-avatar v-else-if="auth.isLoggedIn" :size="44">{{ auth.user?.displayName?.slice(0,1).toUpperCase() || 'U' }}</el-avatar>
          <div v-else class="avatar">😊</div>
          <div class="hello">
            <div class="greet">{{ auth.isLoggedIn ? (auth.user?.displayName || '欢迎回来') : '您好' }}</div>
            <div class="tips">{{ auth.isLoggedIn ? '祝你淘到心仪好物～' : '登录后可享个性化推荐' }}</div>
          </div>
        </div>
        <el-button v-if="!auth.isLoggedIn" type="primary" class="login-btn" @click="onLoginClick">立即登录</el-button>
        <el-button v-else type="primary" class="login-btn" @click="go('/sell')">发布宝贝</el-button>
        <div class="quick">
          <div class="q-item" @click="go('/favorites')"><span>⭐</span> 宝贝收藏</div>
          <div class="q-item" @click="go('/orders')"><span>🛒</span> 买过的</div>
          <div class="q-item" @click="go('/shops/favorites')"><span>❤️</span> 收藏的店</div>
          <div class="q-item" @click="go('/footprints')"><span>🧭</span> 我的足迹</div>
        </div>
        <div class="promo-mini">
          <div class="title">金币购购物抵钱用</div>
          <el-button size="small" type="warning" @click="go('/benefits/coins')">立即领取</el-button>
        </div>
      </div>
    </div>
  </div>
  <div class="section-divider"></div>
  <h2 class="page-title">
    <span class="like-badge"><span class="heart">❤</span> 猜你喜欢</span>
    <span class="tags">
      <span v-for="t in hotTags" :key="t.name" class="tag" :class="{ 'active': selectedHotTag === t.name }" @click="clickHotTag(t.name)">
        <span class="tag-icon">{{ t.emoji }}</span>
        {{ t.name }}
      </span>
    </span>
  </h2>
  <div class="section-divider"></div>
  <div v-if="loadingItems" class="grid loading-grid"><div class="spinner"></div></div>
  <div v-else class="grid">
    <div 
      v-for="item in items" 
      :key="item.id" 
      class="card" 
      :class="{ 'is-own-item': isOwnItem(item) }"
      @click="handleCardClick(item)"
    >
      <div class="card-image-wrapper">
        <img :src="item.image || placeholder(item.id)" alt="" />
        <div 
          v-if="auth.isLoggedIn" 
          class="favorite-star" 
          @click.stop="toggleFavorite(item)"
          :class="{ 'favorited': item.isFavorited }"
        >
          <el-icon :size="20">
            <StarFilled v-if="item.isFavorited" />
            <Star v-else />
          </el-icon>
        </div>
        <div v-if="isOwnItem(item)" class="own-badge">我的商品</div>
      </div>
      <div class="title">{{ item.title }}</div>
      <div class="desc">{{ (item.description || '') }}</div>
      <div class="price">¥{{ item.price }}</div>
    </div>
  </div>
  <div class="pager">
    <el-button @click="prevPage" :disabled="page===1">上一页</el-button>
    <span class="pageinfo">第 {{ page }} 页</span>
    <el-button @click="nextPage" :disabled="!hasNext">下一页</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { useAuthStore } from '../store/auth'
import { StarFilled, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const q = ref('')
const categoryId = ref<number | undefined>(undefined)
const selectedCategoryId = ref<number | undefined>(undefined) // 当前选中的分类ID
const selectedHotTag = ref<string | undefined>(undefined) // 当前选中的热门标签
const selectedHotKey = ref<string | undefined>(undefined) // 当前选中的热门关键词
const router = useRouter()
const auth = useAuthStore()
const items = ref<any[]>([])
const page = ref(1)
const pageSize = ref(20)
const hasNext = ref(false)
const hotKeys = ref<string[]>(['手机','电脑','家具','相机','平板'])
const loadingItems = ref(true)
const loadingCats = ref(true)
const banners = ref<string[]>([
  'https://picsum.photos/seed/banner1/1200/260',
  'https://picsum.photos/seed/banner2/1200/260',
  'https://picsum.photos/seed/banner3/1200/260'
])
const catList = ref<any[]>([])
const topLevelCats = computed(() => {
  return catList.value.filter((c: any) => c.depth === 0).slice(0, 12)
})

const hotTags = ref<any[]>([
  { name:'手机', emoji: '📱' },
  { name:'电脑', emoji: '💻' },
  { name:'相机', emoji: '📷' },
  { name:'耳机', emoji: '🎧' },
  { name:'球鞋', emoji: '👟' },
  { name:'行李箱', emoji: '🧳' },
  { name:'演出票', emoji: '🎫' },
  { name:'热卖', emoji: '🔥' }
])

function placeholder(id:any){ return `https://picsum.photos/seed/home${id}/400/300` }

async function load(){
  loadingItems.value = true
  const limit = pageSize.value
  const offset = (page.value - 1) * pageSize.value
  try{
    const { data } = await http.get('/api/items', { 
      params: { 
        limit, 
        offset, 
        q: q.value || undefined,
        categoryId: categoryId.value || undefined
      } 
    })
    if (data.code === 0) {
      items.value = data.data
      hasNext.value = (Array.isArray(data.data) && data.data.length === limit)
      
      // 如果已登录，批量获取收藏状态
      if (auth.isLoggedIn && items.value.length > 0) {
        await loadFavoriteStatus()
      }
    }
  } finally {
    loadingItems.value = false
  }
}

async function loadFavoriteStatus() {
  if (!auth.isLoggedIn || items.value.length === 0) return
  
  try {
    // 批量检查收藏状态
    const itemIds = items.value.map((item: any) => item.id)
    const checkPromises = itemIds.map((id: number) => 
      http.get(`/api/favorites/items/${id}/status`).catch(() => ({ data: { code: 0, data: { isFavorited: false } } }))
    )
    
    const results = await Promise.all(checkPromises)
    results.forEach((result: any, index: number) => {
      if (result.data?.code === 0 && result.data?.data) {
        items.value[index].isFavorited = result.data.data.isFavorited || false
      } else {
        items.value[index].isFavorited = false
      }
    })
  } catch (error) {
    // 静默失败，不影响商品列表显示
    console.error('Failed to load favorite status:', error)
  }
}

async function toggleFavorite(item: any) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const { data } = await http.post(`/api/favorites/items/${item.id}`)
    if (data.code === 0) {
      item.isFavorited = data.data?.isFavorited || false
      if (item.isFavorited) {
        ElMessage.success('已收藏')
      } else {
        ElMessage.success('已取消收藏')
      }
    }
  } catch (error: any) {
    if (error.response?.status === 401) {
      ElMessage.warning('请先登录')
    } else {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

function search(){ 
  categoryId.value = undefined // 搜索时清空分类筛选
  selectedCategoryId.value = undefined // 清空分类选中状态
  selectedHotTag.value = undefined // 清空标签选中状态
  selectedHotKey.value = undefined // 清空热门关键词选中状态
  page.value = 1; 
  load() 
}
function nextPage(){ if (hasNext.value) { page.value++; load() } }
function prevPage(){ if (page.value>1) { page.value--; load() } }
onMounted(load)

onMounted(async () => { await loadCats() })

async function loadCats(){
  loadingCats.value = true
  const { data } = await http.get('/api/items/categories')
  if (data.code===0){
    const res:any[] = []
    const walk = (nodes:any[], prefix:string[]) => {
      for (const n of nodes){
        const label = [...prefix, n.name].join(' / ')
        res.push({ id:n.id, label, depth: prefix.length })
        if (n.children) walk(n.children, [...prefix, n.name])
      }
    }
    walk(data.data||[], [])
    catList.value = res
  }
  loadingCats.value = false
}

function goCat(c:any){ q.value = c.label; search() }
function lastName(label:string){
  const parts = (label||'').split(' / ')
  return parts[parts.length-1] || label
}
function goTag(c:any){ 
  // 如果点击的是已选中的分类，则取消选中
  if (selectedCategoryId.value === c.id) {
    selectedCategoryId.value = undefined
    categoryId.value = undefined
    selectedHotTag.value = undefined
    selectedHotKey.value = undefined
    q.value = ''
  } else {
    selectedCategoryId.value = c.id
    categoryId.value = c.id // 使用分类ID进行精确匹配
    q.value = '' // 清空搜索关键词
    selectedHotTag.value = undefined // 清空标签选中状态
    selectedHotKey.value = undefined // 清空热门关键词选中状态
  }
  page.value = 1
  load()
}

function clickHotTag(tagName: string) {
  // 如果点击的是已选中的标签，则取消选中
  if (selectedHotTag.value === tagName) {
    selectedHotTag.value = undefined
    q.value = ''
    categoryId.value = undefined
    selectedCategoryId.value = undefined
    selectedHotKey.value = undefined
    page.value = 1
    load()
  } else {
    selectedHotTag.value = tagName
    q.value = tagName
    categoryId.value = undefined // 清空分类筛选
    selectedCategoryId.value = undefined // 清空分类选中状态
    selectedHotKey.value = undefined // 清空热门关键词选中状态
    page.value = 1
    load() // 直接调用load，避免search函数清空状态
  }
}

function clickHotKey(keyName: string) {
  // 如果点击的是已选中的关键词，则取消选中
  if (selectedHotKey.value === keyName) {
    selectedHotKey.value = undefined
    q.value = ''
    categoryId.value = undefined
    selectedCategoryId.value = undefined
    selectedHotTag.value = undefined
    page.value = 1
    load()
  } else {
    selectedHotKey.value = keyName
    q.value = keyName
    categoryId.value = undefined // 清空分类筛选
    selectedCategoryId.value = undefined // 清空分类选中状态
    selectedHotTag.value = undefined // 清空标签选中状态
    page.value = 1
    load() // 直接调用load，避免search函数清空状态
  }
}

function onLoginClick(){
  // 触发全局登录弹窗事件（LoginModal 可监听该事件）
  try { window.dispatchEvent(new CustomEvent('show-login')) } catch(e) {}
}

function go(path:string){ router.push(path) }

// 判断是否是本人上架的商品
function isOwnItem(item: any): boolean {
  if (!auth.isLoggedIn || !auth.user?.userId || !item.seller?.id) return false
  return item.seller.id === auth.user.userId
}

// 处理卡片点击事件（允许自己的商品也进入详情页，只是禁用购买）
function handleCardClick(item: any) {
  router.push(`/item/${item.id}`)
}

function emojiFor(name:string){
  const map:Record<string,string> = {
    // 顶级类目
    '数码3C':'📟','电子产品':'🔋','居家生活':'🏠','家居生活':'🛋️','服饰鞋包':'👗','家用电器':'🔌','运动健康':'🏀','户外旅行':'⛺','乐器音乐':'🎸','收藏文玩':'🧿','游戏电竞':'🎮','图书文创':'📚','母婴用品':'🍼','宠物用品':'🐾','出行交通':'🛵','办公设备':'🗄️','摄影影音':'📷','潮流玩具':'🧸','手办模型':'🤖','票券卡类':'🎫','其他':'◻️',
    // 常见具体类目
    '手机':'📱','电脑':'💻','家具':'🛋️',
    '平板':'📱','相机':'📷','影音设备':'🔊','可穿戴设备':'⌚','数码配件':'🔌',
    '冰洗空':'🧊','厨房电器':'🍳','清洁电器':'🧹',
    '家居日用':'🏠','收纳整理':'🗃️','灯具照明':'💡',
    '男装':'👔','女装':'👗','鞋靴':'👞','箱包':'🧳',
    '运动装备':'🏀','健身器材':'🏋️','露营登山':'🏕️','骑行滑板':'🚴',
    '单反/微单':'📷','镜头':'🎯','录音/监听':'🎧',
    '主机/掌机':'🕹️','游戏周边':'🎮',
    '文学艺术':'📖','教材教辅':'📘','漫画杂志':'📙'
  }
  return map[name] || '◯'
}
</script>

<style scoped>
.page-title{padding:12px 16px;font-size:18px;font-weight:700}
.page-title .like-badge{display:inline-flex;align-items:center;gap:6px;background:#ff8a00;color:#fff;border-radius:18px;padding:4px 10px;margin-right:10px}
.page-title .heart{color:#ff5a7a;margin-right:6px}
.page-title .tags{margin-left:12px;display:inline-flex;gap:8px;flex-wrap:wrap;vertical-align:middle}
.page-title .tag{display:inline-flex;align-items:center;gap:4px;background:#fff;border:1px solid #eee;color:#666;border-radius:16px;padding:4px 10px;font-size:12px;cursor:pointer;transition:all 0.3s}
.page-title .tag:not(.active):hover{border-color:#ff5a7a;color:#ff5a7a}
.page-title .tags .tag.active{background:#ff5a7a !important;border-color:#ff5a7a !important;color:#fff !important}
.page-title .tags .tag.active:hover{background:#ff4d4f !important;border-color:#ff4d4f !important;color:#fff !important}
.page-title .tag-icon{width:14px;height:14px}
.section-divider{height:2px;background:#ff4d4f;margin:0 16px}
.tao-header{
  position: relative;
  background: 
    linear-gradient(135deg, #ff3a5b 0%, #ff5a7a 25%, #ff7e7e 50%, #ff5a7a 75%, #ff3a5b 100%);
  background-size: 400% 400%;
  animation: gradientFlow 8s ease infinite;
  overflow: hidden;
}
@keyframes gradientFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
.tao-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  animation: shimmer 3s infinite;
}
.tao-header::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 20%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  animation: pulse 4s ease-in-out infinite;
  pointer-events: none;
}
@keyframes shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}
@keyframes pulse {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}
.tao-header .container{max-width:1200px;margin:0 auto;display:flex;align-items:center;gap:16px;padding:8px 16px;color:#fff;position:relative;z-index:1}
.tao-header .logo{font-size:20px;font-weight:800;letter-spacing:.5px}
.tao-header .logo span{font-weight:600}
.tao-header .search-wrap{flex:1}
.tao-header .search-input :deep(.el-input__inner){border-top-left-radius:20px;border-bottom-left-radius:20px}
.tao-header .search-btn{border-top-right-radius:20px;border-bottom-right-radius:20px}
.tao-header .hot-links{margin-top:6px;display:flex;gap:12px}
.tao-header .hot-links :deep(.el-link){color:#ffe9ec;transition:all 0.3s;padding:4px 8px;border-radius:12px}
.tao-header .hot-links :deep(.el-link:not(.active):hover){color:#fff;background:rgba(255,255,255,0.2)}
.tao-header .hot-links :deep(.el-link.active){color:#fff !important;background:rgba(255,255,255,0.3) !important;font-weight:600}
.tao-header .hot-links :deep(.el-link.active:hover){background:rgba(255,255,255,0.4) !important}
.promo{background:#fff}
.promo .container{max-width:1800px;margin:0 auto;padding:12px 16px}
.promo-layout{display:grid;grid-template-columns:320px minmax(0,1fr) 320px;gap:20px;align-items:start}
.promo .cats{border:1px solid #eee;border-radius:12px;background:#f7f7f7;padding:0 0 12px}
.login-card{width:100%;border:1px solid #eee;border-radius:12px;background:#f7f7f7;padding:12px;display:flex;flex-direction:column;gap:10px}
.promo .carousel{max-width:none;margin:0}
.login-card .user-row{display:flex;align-items:center;gap:10px}
.login-card .avatar{width:44px;height:44px;border-radius:50%;background:#fff3e8;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0}
.login-card .greet{font-weight:700;color:#333}
.login-card .tips{color:#999;font-size:12px}
.login-card .login-btn{width:100%;margin-top:4px}
.login-card .quick{display:grid;grid-template-columns:repeat(4,1fr);gap:6px;margin-top:4px;color:#555;font-size:12px;text-align:center}
.login-card .q-item{background:#fafafa;border:1px solid #f0f0f0;border-radius:8px;padding:6px 4px;display:flex;flex-direction:column;gap:4px;align-items:center}
.login-card .promo-mini{margin-top:auto;background:#fff8e6;border:1px dashed #ffd666;border-radius:8px;padding:10px;display:flex;align-items:center;justify-content:space-between}
.login-card .promo-mini .title{color:#a36b00;font-size:12px}
.cats-title{padding:10px 16px;font-weight:700;border-bottom:1px solid #f0f0f0;background:#fafafa}
.cat-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;padding:12px}
.cat-tag{background:#fff;border:1px solid #eee;border-radius:14px;padding:6px 10px;font-size:12px;color:#333;cursor:pointer;display:flex;align-items:flex-start;gap:8px;transition:all 0.3s;min-height:38px}
.cat-tag:not(.active):hover{border-color:#409eff;color:#409eff;background:#f5f7ff}
.cat-tag.active{background:#409eff !important;border-color:#409eff !important;color:#fff !important}
.cat-tag.active:hover{background:#66b1ff !important;border-color:#66b1ff !important;color:#fff !important}
.tag-emoji{font-size:16px;line-height:1}
.tag-name{flex:1;white-space:normal;line-height:1.3;word-break:break-word;text-align:left}
.promo .carousel{width:100%}
.promo .banner{width:100%;height:260px;object-fit:cover;border-radius:8px}
.hero{display:none}
.grid{display:grid;gap:12px;padding:16px;grid-template-columns:repeat(auto-fill,minmax(200px,1fr))}
.loading-grid{min-height:160px;display:flex;align-items:center;justify-content:center}
.card{background:#fff;border:1px solid #eee;border-radius:8px;overflow:hidden;cursor:pointer;position:relative;transition:all 0.3s}
.card.is-own-item{opacity:0.85;border-color:#d9d9d9}
.card.is-own-item:hover{opacity:0.9}
.card-image-wrapper{position:relative;width:100%;height:140px;overflow:hidden}
.card img{width:100%;height:140px;object-fit:cover}
.favorite-star{position:absolute;top:8px;right:8px;width:32px;height:32px;border-radius:50%;background:rgba(255,255,255,0.9);display:flex;align-items:center;justify-content:center;cursor:pointer;transition:all 0.3s;z-index:10;box-shadow:0 2px 4px rgba(0,0,0,0.1)}
.favorite-star:hover{background:#fff;transform:scale(1.1);box-shadow:0 2px 8px rgba(0,0,0,0.15)}
.favorite-star.favorited{background:#fff5f5}
.favorite-star.favorited :deep(svg){color:#ff4d4f}
.favorite-star:not(.favorited) :deep(svg){color:#999}
.own-badge{position:absolute;top:8px;left:8px;background:rgba(64,158,255,0.9);color:#fff;padding:4px 8px;border-radius:4px;font-size:12px;font-weight:600;z-index:5;box-shadow:0 2px 4px rgba(0,0,0,0.2)}
.title{padding:8px 10px;font-size:14px;line-height:18px;height:36px;overflow:hidden}
.desc{padding:0 10px 6px;color:#777;font-size:12px;line-height:16px;height:32px;overflow:hidden}
.price{padding:0 10px 10px;color:#f04848;font-weight:700}
.pager{display:flex;justify-content:center;align-items:center;gap:12px;padding:16px}
.pageinfo{color:#666}

.spinner-wrap{display:flex;align-items:center;justify-content:center;height:220px}
.spinner{width:24px;height:24px;border:3px solid #e5e5e5;border-top-color:#409eff;border-radius:50%;animation:spin 1s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
</style>



