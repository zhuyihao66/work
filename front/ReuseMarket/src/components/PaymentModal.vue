<template>
  <el-dialog
    v-model="dialogVisible"
    title="扫码支付（测试）"
    width="460px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="order" class="payment-modal">
      <div class="amount-row">
        <div class="label">订单号</div>
        <div class="value">{{ order.orderNo }}</div>
      </div>
      <div class="amount-highlight">
        需支付 <span class="currency">{{ order.currency || '¥' }}</span
        ><span class="amount">{{ formatAmount(order.totalAmount) }}</span>
      </div>

      <div class="countdown" :class="{ danger: remainingSeconds <= 10, expired: remainingSeconds === 0 }">
        <el-icon v-if="remainingSeconds > 0"><Timer /></el-icon>
        <el-icon v-else><WarningFilled /></el-icon>
        <span v-if="remainingSeconds > 0">剩余支付时间：{{ formatCountdown(remainingSeconds) }}</span>
        <span v-else>订单已超时自动取消，请重新下单</span>
      </div>

      <el-radio-group v-model="provider" class="provider-switch" size="large">
        <el-radio-button label="alipay">支付宝</el-radio-button>
        <el-radio-button label="wechat">微信支付</el-radio-button>
      </el-radio-group>

      <div class="qr-panel">
        <div class="qr-frame">
          <img :src="qrUrl" alt="QR" />
          <div class="scan-line"></div>
        </div>
        <div class="qr-tips">
          <div class="tip-title">{{ provider === 'alipay' ? '支付宝' : '微信支付' }}</div>
          <p>请使用{{ provider === 'alipay' ? '支付宝' : '微信' }}扫一扫，完成支付</p>
          <ul>
            <li>本页面仅为演示，实际不会扣款</li>
            <li>支付成功后会自动刷新订单状态</li>
          </ul>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">稍后再付</el-button>
        <el-button
          type="primary"
          :disabled="!order || remainingSeconds === 0"
          :loading="paying"
          @click="handleMockPay"
        >
          模拟支付成功
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Timer, WarningFilled } from '@element-plus/icons-vue'
import { usePaymentStore } from '../store/payment'
import http from '../api/http'
import { ElMessage } from 'element-plus'

const paymentStore = usePaymentStore()
const router = useRouter()
const paying = ref(false)

const dialogVisible = computed({
  get: () => paymentStore.visible,
  set: (val: boolean) => {
    if (!val) {
      handleClose()
    }
  }
})

const order = computed(() => paymentStore.order)
const remainingSeconds = computed(() => paymentStore.remainingSeconds)
const provider = computed({
  get: () => paymentStore.provider,
  set: (val: 'alipay' | 'wechat') => paymentStore.switchProvider(val)
})

const qrUrl = computed(() => {
  if (!order.value) return ''
  const payload = encodeURIComponent(
    JSON.stringify({
      provider: provider.value,
      orderNo: order.value.orderNo,
      amount: order.value.totalAmount
    })
  )
  return `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${payload}`
})

function formatAmount(value?: number) {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

function formatCountdown(sec: number) {
  const mm = String(Math.floor(sec / 60)).padStart(2, '0')
  const ss = String(sec % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

function handleClose() {
  paymentStore.close()
  router.push('/orders')
}

async function handleMockPay() {
  if (!order.value) return
  if (remainingSeconds.value === 0) {
    ElMessage.warning('订单已超时，请重新下单')
    return
  }
  paying.value = true
  try {
    const { data } = await http.post(`/api/orders/${order.value.id}/pay`)
    if (data.code === 0) {
      ElMessage.success('支付成功')
      window.dispatchEvent(new CustomEvent('orders:refresh'))
      handleClose()
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '支付失败')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.payment-modal {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
}
.amount-highlight {
  background: linear-gradient(120deg, #fff2e2, #ffe7c3);
  border-radius: 12px;
  padding: 12px 18px;
  font-size: 16px;
  font-weight: 600;
  color: #a65e00;
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.amount-highlight .currency {
  font-size: 16px;
}
.amount-highlight .amount {
  font-size: 26px;
  color: #f04848;
}
.countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f5f5f5;
  color: #555;
}
.countdown.danger {
  background: #fff2e8;
  color: #d4380d;
}
.countdown.expired {
  background: #fff1f0;
  color: #cf1322;
}
.provider-switch {
  width: 100%;
}
.qr-panel {
  display: flex;
  gap: 16px;
  align-items: center;
}
.qr-frame {
  position: relative;
  width: 220px;
  height: 220px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}
.qr-frame img {
  width: 210px;
  height: 210px;
}
.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, transparent, rgba(255, 215, 0, 0.9), transparent);
  animation: scan 2s linear infinite;
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.7);
}
@keyframes scan {
  0% {
    transform: translateY(-4px);
  }
  100% {
    transform: translateY(220px);
  }
}
.qr-tips {
  flex: 1;
  font-size: 13px;
  color: #666;
}
.qr-tips .tip-title {
  font-weight: 600;
  margin-bottom: 4px;
}
.qr-tips ul {
  padding-left: 18px;
  margin: 6px 0 0 0;
  color: #999;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>


