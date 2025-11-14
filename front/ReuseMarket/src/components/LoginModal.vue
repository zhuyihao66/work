<template>
  <el-dialog v-model="visibleInner" title="手机号登录" width="420px" :close-on-click-modal="false" @closed="onMainClosed">
    <div class="form">
      <el-input v-model="phone" placeholder="请输入手机号" maxlength="11" />
      <div class="code-row">
        <el-input v-model="code" placeholder="短信验证码" maxlength="6" />
        <el-button :disabled="countdown>0 || !isPhoneValid" @click="openCaptcha">
          {{ countdown>0 ? `${countdown}s` : '获取短信码' }}
        </el-button>
      </div>
      <el-button type="primary" :disabled="!isReady" :loading="loading" @click="verify">登录 / 自动注册</el-button>
      <div class="tip">未注册手机号将自动创建账户</div>
    </div>
  </el-dialog>

  <!-- 图形验证码弹窗 -->
  <el-dialog v-model="showCaptcha" title="安全验证" width="360px" :close-on-click-modal="false" @closed="onCaptchaClosed">
    <div class="captcha-wrap">
      <el-input v-model="captcha" placeholder="请输入图形验证码" maxlength="6" />
      <img v-if="captchaImg" :src="captchaImg" class="captcha" @click="loadCaptcha" title="点击刷新" />
      <div v-else class="captcha spinner" title="加载中">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
    </div>
    <template #footer>
      <el-button @click="showCaptcha=false">取消</el-button>
      <el-button type="primary" :disabled="!isCaptchaOk" :loading="sending" @click="sendCode">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import http from '../api/http'

const router = useRouter()

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits(['update:visible'])
const visibleInner = ref(props.visible)
watch(() => props.visible, v => visibleInner.value = v)
watch(visibleInner, v => emit('update:visible', v))

const phone = ref('')
const captcha = ref('')
const captchaId = ref('')
const captchaImg = ref('')
const showCaptcha = ref(false)
const sending = ref(false)
const code = ref('')
const countdown = ref(0)
const loading = ref(false)
const isPhoneValid = computed(() => /^1\d{10}$/.test(phone.value))
const isCaptchaOk = computed(() => /^[a-zA-Z0-9]{4,6}$/.test(captcha.value) && !!captchaId.value)
const isReady = computed(() => isPhoneValid.value && /^\d{6}$/.test(code.value))
let timer: number | undefined

function openCaptcha(){
  if (!isPhoneValid.value || countdown.value>0) return
  loadCaptcha()
  showCaptcha.value = true
}

async function sendCode(){
  if (!isPhoneValid.value || !isCaptchaOk.value || countdown.value>0) return
  sending.value = true
  try{
    await http.post('/api/auth/otp/send', { phone: phone.value, captchaId: captchaId.value, captcha: captcha.value })
    countdown.value = 60
    timer = window.setInterval(()=>{
      countdown.value--
      if (countdown.value<=0 && timer){ clearInterval(timer) }
    },1000)
    ElMessage.success('短信验证码已发送')
    showCaptcha.value = false
  }catch(e:any){
    ElMessage.error(e?.response?.data?.message || '发送失败')
    loadCaptcha()
  } finally {
    sending.value = false
  }
}

const auth = useAuthStore()
async function verify(){
  if (!isReady.value) return
  loading.value = true
  try {
    const { data } = await http.post('/api/auth/otp/verify', { phone: phone.value, code: code.value })
    if (data.code !== 0) throw new Error(data.message)
    const info = data.data
    // 先设置基本认证信息
    auth.setAuth(info.token, { userId: info.userId, displayName: info.displayName, phone: phone.value })
    
    // 登录成功后获取完整用户信息（包括头像）
    try {
      const meRes = await http.get('/api/me')
      if (meRes.data.code === 0 && meRes.data.data) {
        const userInfo = meRes.data.data
        // 更新auth store中的用户信息，包含头像
        auth.setAuth(info.token, { 
          userId: info.userId, 
          displayName: userInfo.displayName || info.displayName, 
          phone: userInfo.phone || phone.value,
          avatarUrl: userInfo.avatarUrl
        })
      }
    } catch (e) {
      // 如果获取用户信息失败，不影响登录流程
      console.warn('Failed to fetch user info:', e)
    }
    
    visibleInner.value = false
    ElMessage.success('登录成功')
    
    // 检查是否有需要返回的路由路径
    const returnPath = auth.getAndClearReturnPath()
    if (returnPath) {
      // 导航到原路由
      await router.push(returnPath)
    }
    // 登录成功后始终刷新页面以重新加载数据
    window.location.reload()
  } catch (e:any) {
    ElMessage.error(e?.response?.data?.message || e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function loadCaptcha(){
  try{
    const { data } = await http.get('/api/auth/captcha/new')
    if (data.code === 0){
      captchaId.value = data.data.id
      captchaImg.value = data.data.imageBase64
      captcha.value = ''
    }
  }catch{}
}

function clearTimer(){ if (timer){ clearInterval(timer); timer = undefined } }

function resetCaptcha(){
  captcha.value = ''
  captchaId.value = ''
  captchaImg.value = ''
}

function resetAll(){
  code.value = ''
  countdown.value = 0
  clearTimer()
  resetCaptcha()
}

function onMainClosed(){
  resetAll()
}

function onCaptchaClosed(){
  resetCaptcha()
}

// 当打开图形验证码弹窗时加载
watch(() => showCaptcha.value, v => { if (v) loadCaptcha() })

onBeforeUnmount(()=>{ clearTimer() })
</script>

<style scoped>
.form{display:flex;flex-direction:column;gap:12px}
.code-row{display:flex;gap:8px}
.captcha-wrap{display:flex;gap:8px;align-items:center}
.captcha{height:38px;border:1px solid #eee;border-radius:6px;cursor:pointer}
.spinner{display:flex;align-items:center;justify-content:center;width:100px}
</style>


