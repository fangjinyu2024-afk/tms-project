package com.zxinfotek.tms.infra.excel;

import com.alibaba.excel.EasyExcel;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.infra.i18n.I18nMessages;
import com.zxinfotek.tms.infra.storage.FileStorage;
import com.zxinfotek.tms.infra.storage.StorageProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 列表导出：按当前筛选条件与请求语言生成 Excel 后存入对象存储，返回带有效期的下载链接（详细设计 7.11）。
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

    /**
     * 生成导出文件：表头与工作表名按消息键解析为当前请求语言，行数据由调用方按表头顺序提供。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public String export(String bizType, Long tenantId, String sheetNameKey,
                         List<String> headerKeys, List<List<Object>> rows) {
        List<List<String>> head = new ArrayList<>(headerKeys.size());
        for (String headerKey : headerKeys) {
            head.add(List.of(I18nMessages.get(headerKey)));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out).head(head).sheet(I18nMessages.get(sheetNameKey)).doWrite(rows);

        String objectId = bizType + "-" + STAMP.format(UtcTimes.now());
        String objectKey = fileStorage.buildObjectKey("export", tenantId, objectId, ".xlsx");
        fileStorage.upload(objectKey, out.toByteArray(), CONTENT_TYPE);
        return fileStorage.presignedUrl(objectKey, Duration.ofMinutes(properties.getPresignedMinutes()));
    }
}
