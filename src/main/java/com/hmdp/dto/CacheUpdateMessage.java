package com.hmdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // 消息类型：UPDATE(更新)、DELETE(删除)
    private String type;
    // 缓存key
    private String cacheKey;
    // 缓存值（更新时使用）
    private Object cacheValue;
    // 过期时间（秒）
    private Long expireTime;
    // 重试次数
    private Integer retryCount = 0;
    // 最大重试次数
    private static final Integer MAX_RETRY_COUNT = 3;

    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";

    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}