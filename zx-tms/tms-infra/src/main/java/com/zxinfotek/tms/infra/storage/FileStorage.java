package com.zxinfotek.tms.infra.storage;

import java.io.InputStream;
import java.time.Duration;

/**
 * 对象存储契约，升级包、导入文件与导出文件统一经此存取（详细设计 7.11）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public interface FileStorage {

    String upload(String objectKey, byte[] content, String contentType);

    InputStream download(String objectKey);

    void delete(String objectKey);

    /** 返回带有效期的预签名链接，后台下载与导出一律走该链接，不直接暴露对象存储凭据。 */
    String presignedUrl(String objectKey, Duration ttl);

    /** 按「类型/客户/年月/对象ID」组织对象键，不使用原始文件名。 */
    String buildObjectKey(String type, Long tenantId, String objectId, String extension);
}
