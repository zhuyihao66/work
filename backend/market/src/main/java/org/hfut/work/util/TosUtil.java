package org.hfut.work.util;

import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.TosClientException;
import com.volcengine.tos.TosServerException;
import com.volcengine.tos.model.object.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 火山引擎TOS对象存储工具类
 * 用于文件上传、下载、删除和列表
 */
@Component
public class TosUtil {

    @Value("${app.tos.endpoint:}")
    private String endpoint;

    @Value("${app.tos.region:}")
    private String region;

    @Value("${app.tos.accessKey:}")
    private String accessKey;

    @Value("${app.tos.secretKey:}")
    private String secretKey;

    @Value("${app.tos.bucketName:}")
    private String bucketName;

    @Value("${app.tos.domain:}")
    private String domain; // CDN域名或存储桶域名，用于返回可访问的URL

    private TOSV2 tosClient;

    @PostConstruct
    public void init() {
        if (endpoint == null || endpoint.isEmpty() ||
            region == null || region.isEmpty() ||
            accessKey == null || accessKey.isEmpty() ||
            secretKey == null || secretKey.isEmpty() ||
            bucketName == null || bucketName.isEmpty()) {
            throw new IllegalStateException("TOS配置不完整，请检查application.yml中的app.tos配置");
        }
        // TOSV2ClientBuilder.build(region, endpoint, accessKey, secretKey)
        tosClient = new TOSV2ClientBuilder().build(region, endpoint, accessKey, secretKey);
    }

    /**
     * 上传文件到TOS（设置为公共读）
     * @param inputStream 文件输入流
     * @param objectKey 对象键（文件路径），例如：uploads/20250103/uuid.jpg
     * @param contentType 文件类型，例如：image/jpeg（可选，SDK会自动推断）
     * @return 可访问的URL
     * @throws TosClientException 客户端异常
     * @throws TosServerException 服务端异常
     */
    public String uploadFile(InputStream inputStream, String objectKey, String contentType) 
            throws TosClientException, TosServerException {
        PutObjectInput putObjectInput = new PutObjectInput()
                .setBucket(bucketName)
                .setKey(objectKey)
                .setContent(inputStream);

        tosClient.putObject(putObjectInput);
        
        // 返回可访问的URL（存储桶已配置为公共读）
        if (domain != null && !domain.isEmpty()) {
            // 如果配置了CDN域名，使用CDN域名
            String url = domain.endsWith("/") ? domain : domain + "/";
            return url + objectKey;
        } else {
            // 使用存储桶默认域名
            return String.format("https://%s.%s/%s", bucketName, endpoint, objectKey);
        }
    }

    /**
     * 下载文件从TOS
     * @param objectKey 对象键（文件路径）
     * @return 文件输入流，需要调用者关闭
     * @throws TosClientException 客户端异常
     * @throws TosServerException 服务端异常
     */
    public InputStream downloadFile(String objectKey) 
            throws TosClientException, TosServerException {
        GetObjectV2Input input = new GetObjectV2Input()
                .setBucket(bucketName)
                .setKey(objectKey);
        
        GetObjectV2Output output = tosClient.getObject(input);
        return output.getContent();
    }

    /**
     * 删除文件
     * @param objectKey 对象键（文件路径）
     * @throws TosClientException 客户端异常
     * @throws TosServerException 服务端异常
     */
    public void deleteFile(String objectKey) 
            throws TosClientException, TosServerException {
        DeleteObjectInput deleteObjectInput = new DeleteObjectInput()
                .setBucket(bucketName)
                .setKey(objectKey);
        
        tosClient.deleteObject(deleteObjectInput);
    }

    /**
     * 检查文件是否存在（通过尝试下载元数据）
     * @param objectKey 对象键（文件路径）
     * @return 是否存在
     */
    public boolean fileExists(String objectKey) {
        try {
            // 尝试获取对象元数据，如果不存在会抛出异常
            GetObjectV2Input input = new GetObjectV2Input()
                    .setBucket(bucketName)
                    .setKey(objectKey);
            // 只读取元数据，不读取内容
            try (GetObjectV2Output output = tosClient.getObject(input)) {
                return true;
            }
        } catch (TosServerException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 列举对象（支持分页）
     * @param prefix 对象键前缀，例如：uploads/20250103/
     * @param maxKeys 最大返回数量，默认1000
     * @return 对象键列表
     */
    public List<String> listObjects(String prefix, int maxKeys) 
            throws TosClientException, TosServerException {
        List<String> objectKeys = new ArrayList<>();
        ListObjectsType2Input input = new ListObjectsType2Input()
                .setBucket(bucketName)
                .setPrefix(prefix)
                .setMaxKeys(maxKeys);
        
        ListObjectsType2Output output = tosClient.listObjectsType2(input);
        
        if (output.getContents() != null) {
            for (ListedObjectV2 object : output.getContents()) {
                objectKeys.add(object.getKey());
            }
        }
        
        // 如果结果被截断，继续列举
        while (output.isTruncated() && output.getNextContinuationToken() != null) {
            input.setContinuationToken(output.getNextContinuationToken());
            output = tosClient.listObjectsType2(input);
            
            if (output.getContents() != null) {
                for (ListedObjectV2 object : output.getContents()) {
                    objectKeys.add(object.getKey());
                }
            }
        }
        
        return objectKeys;
    }

    /**
     * 根据URL提取objectKey
     * 如果URL是完整域名，提取路径部分；如果已经是路径，直接返回
     */
    public String extractObjectKeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // 如果是完整URL，提取路径部分
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // 移除协议和域名，保留路径
            int idx = url.indexOf('/', url.indexOf("//") + 2);
            if (idx > 0) {
                return url.substring(idx + 1);
            }
        }
        
        // 如果是相对路径，移除开头的/
        if (url.startsWith("/")) {
            return url.substring(1);
        }
        
        return url;
    }
}

