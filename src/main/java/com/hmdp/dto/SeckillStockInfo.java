package com.hmdp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillStockInfo {
    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 秒杀开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 秒杀结束时间
     */
    private LocalDateTime endTime;

    public SeckillStockInfo() {}

    public SeckillStockInfo(Integer stock, LocalDateTime beginTime, LocalDateTime endTime) {
        this.stock = stock;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
}
