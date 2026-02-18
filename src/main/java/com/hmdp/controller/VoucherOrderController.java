package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.ratelimiter.RateLimiter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    /**
     * 秒杀优惠券
     * @param voucherId 优惠券ID
     * @return 订单ID
     */
    @RateLimiter(
            window = 10,
            limit = 10,
            message = "秒杀活动太火爆，请稍后再试"
    )
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        // return voucherOrderService.seckillVoucher(voucherId);
        return Result.ok("秒杀成功");
    }
}
