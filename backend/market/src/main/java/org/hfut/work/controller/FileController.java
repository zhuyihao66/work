package org.hfut.work.controller;

import org.hfut.work.common.ApiResponse;
import org.hfut.work.util.TosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.volcengine.tos.TosClientException;
import com.volcengine.tos.TosServerException;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private TosUtil tosUtil;

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.error(400, "文件为空");
        }
        
        try {
            // 生成文件路径：uploads/yyyymmdd/uuid.ext
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String dir = LocalDate.now().toString().replace("-", "");
            String filename = UUID.randomUUID().toString().replace("-", "");
            if (ext != null && !ext.isEmpty()) {
                filename += "." + ext.toLowerCase();
            }
            String objectKey = "uploads/" + dir + "/" + filename;
            
            // 上传到TOS
            String url = tosUtil.uploadFile(
                file.getInputStream(), 
                objectKey, 
                file.getContentType()
            );
            
            Map<String, String> body = new HashMap<>();
            body.put("url", url);
            return ApiResponse.ok(body);
        } catch (TosClientException e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        } catch (TosServerException e) {
            return ApiResponse.error(e.getStatusCode(), "上传失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 代理访问TOS文件（解决私有存储桶访问问题）
     * @param key 对象键（URL编码）
     * @param response HTTP响应
     */
    @GetMapping("/proxy")
    public void proxy(@RequestParam String key, HttpServletResponse response) {
        try {
            // 解码URL
            String objectKey = java.net.URLDecoder.decode(key, "UTF-8");
            
            // 从TOS下载文件
            InputStream inputStream = tosUtil.downloadFile(objectKey);
            
            // 设置响应头
            String contentType = "application/octet-stream";
            if (objectKey.endsWith(".jpg") || objectKey.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (objectKey.endsWith(".png")) {
                contentType = "image/png";
            } else if (objectKey.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (objectKey.endsWith(".webp")) {
                contentType = "image/webp";
            }
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=3600");
            
            // 复制流到响应
            try (OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}


