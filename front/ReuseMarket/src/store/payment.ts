import { defineStore } from 'pinia'

export interface PaymentOrderInfo {
  id: number
  orderNo: string
  totalAmount: number
  currency?: string
  expiresAt?: string
  remainingSeconds?: number
  sellerId?: number
}

interface PaymentState {
  visible: boolean
  order: PaymentOrderInfo | null
  provider: 'alipay' | 'wechat'
  remainingSeconds: number
  timerId: number | null
}

export const usePaymentStore = defineStore('payment', {
  state: (): PaymentState => ({
    visible: false,
    order: null,
    provider: 'alipay',
    remainingSeconds: 0,
    timerId: null
  }),
  actions: {
    open(order: PaymentOrderInfo) {
      this.order = {
        ...order,
        totalAmount: Number(order.totalAmount || 0)
      }
      this.provider = 'alipay'
      this.visible = true
      this.setCountdown(order.expiresAt, order.remainingSeconds)
    },
    close() {
      this.visible = false
      this.order = null
      this.remainingSeconds = 0
      this.clearTimer()
    },
    switchProvider(next: 'alipay' | 'wechat') {
      this.provider = next
    },
    syncCountdown(orderId: number, seconds: number) {
      if (!this.order || this.order.id !== orderId) return
      this.remainingSeconds = Math.max(0, Math.floor(seconds))
      if (this.remainingSeconds === 0) {
        this.clearTimer()
      }
    },
    setCountdown(expiresAt?: string, presetSeconds?: number) {
      this.clearTimer()
      if (!expiresAt && (presetSeconds === undefined || presetSeconds === null)) {
        this.remainingSeconds = 0
        return
      }
      if (presetSeconds !== undefined && presetSeconds !== null) {
        this.remainingSeconds = Math.max(0, Math.floor(presetSeconds))
      } else if (expiresAt) {
        const diff = Math.floor((new Date(expiresAt).getTime() - Date.now()) / 1000)
        this.remainingSeconds = Math.max(0, diff)
      }
      if (this.remainingSeconds > 0) {
        this.timerId = window.setInterval(() => {
          if (this.remainingSeconds > 0) {
            this.remainingSeconds--
          }
          if (this.remainingSeconds <= 0) {
            this.clearTimer()
          }
        }, 1000)
      }
    },
    markExpired(orderId?: number) {
      if (orderId && this.order && this.order.id !== orderId) return
      this.remainingSeconds = 0
      this.clearTimer()
    },
    clearTimer() {
      if (this.timerId) {
        clearInterval(this.timerId)
        this.timerId = null
      }
    }
  }
})


