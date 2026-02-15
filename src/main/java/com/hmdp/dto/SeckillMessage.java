package com.hmdp.dto;

import lombok.Data;

@Data
public class SeckillMessage {
    private Long userId;
    private Long voucherId;
    private Long orderId;

    public SeckillMessage(){}

    public SeckillMessage(Long userId,Long voucherId,Long orderId){
        this.userId = userId;
        this.voucherId = voucherId;
        this.orderId = orderId;
    }
}
