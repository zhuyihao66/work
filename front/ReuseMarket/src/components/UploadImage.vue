<template>
  <el-upload
    class="uploader"
    :action="action"
    :headers="headers"
    name="file"
    :limit="1"
    list-type="picture-card"
    :on-success="onSuccess"
    :on-remove="onRemove"
    :before-upload="beforeUpload"
    :file-list="fileList">
    <el-icon><Plus /></el-icon>
  </el-upload>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import http from '../api/http'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const props = defineProps<{ modelValue?: string }>()
const emit = defineEmits(['update:modelValue'])
const auth = useAuthStore()
const action = `${(http.defaults.baseURL || '').replace(/\/$/, '')}/api/files/upload`
const headers = computed(() => auth.token ? { Authorization: `Bearer ${auth.token}` } : {})
const fileList = ref<any[]>([])

watch(() => props.modelValue, (v) => {
  fileList.value = v ? [{ name: 'image', url: v }] : []
}, { immediate: true })

function onSuccess(res:any){
  if (res && res.code===0 && res.data?.url) emit('update:modelValue', res.data.url)
}
function onRemove(){ emit('update:modelValue', '') }
function beforeUpload(file: File){
  const ok = file.type.startsWith('image/') && file.size <= 5 * 1024 * 1024
  if (!ok) ElMessage.error('仅支持图片且不超过5MB')
  return ok
}
</script>

<style scoped>
.uploader :deep(.el-upload){
  --el-upload-picture-card-size: 96px;
}
</style>


