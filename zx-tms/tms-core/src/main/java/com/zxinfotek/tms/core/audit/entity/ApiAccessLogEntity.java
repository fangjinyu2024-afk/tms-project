package com.zxinfotek.tms.core.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_api_access_log")
public class ApiAccessLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String traceId;
    private Long memberId;
    private String method;
    private String path;
    private Integer httpStatus;
    private String bizCode;
    private Integer durationMs;
    private String clientIp;
    private LocalDateTime createTime;
}
