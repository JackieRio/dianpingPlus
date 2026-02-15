package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.dto.SeckillMessage;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    void createVoucherOrder(SeckillMessage message);

    @Transactional
    void addSeckillVoucher(Voucher voucher);
}
