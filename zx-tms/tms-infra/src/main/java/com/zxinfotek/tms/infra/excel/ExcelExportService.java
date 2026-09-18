package com.zxinfotek.tms.infra.excel;

import com.alibaba.excel.EasyExcel;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.infra.storage.FileStorage;
import com.zxinfotek.tms.infra.storage.StorageProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 列表导出：按当前筛选条件生成 Excel 后存入对象存储，返回带有效期的下载链接（详细设计 7.11）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class ExcelExportService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final FileStorage fileStorage;
    private final StorageProperties properties;

    public ExcelExportService(FileStorage fileStorage, StorageProperties properties) {
        this.fileStorage = fileStorage;
        this.properties = properties;
    }

    public <T> String export(String bizType, Long tenantId, String sheetName, Class<T> rowType, List<T> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, rowType).sheet(sheetName).doWrite(rows);
        String objectId = bizType + "-" + STAMP.format(UtcTimes.now());
        String objectKey = fileStorage.buildObjectKey("export", tenantId, objectId, ".xlsx");
        fileStorage.upload(objectKey, out.toByteArray(), CONTENT_TYPE);
        return fileStorage.presignedUrl(objectKey, Duration.ofMinutes(properties.getPresignedMinutes()));
    }
}
