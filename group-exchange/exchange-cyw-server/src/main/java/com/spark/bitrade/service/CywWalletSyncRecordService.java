package com.spark.bitrade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spark.bitrade.entity.CywWalletSyncRecord;
import com.spark.bitrade.entity.dto.WalletSyncCountDto;

import java.util.Date;

/**
 * 账户WAL流水同步记录表(CywWalletSyncRecord)表服务接口
 *
 * @author archx
 * @since 2019-09-02 14:44:18
 */
public interface CywWalletSyncRecordService extends IService<CywWalletSyncRecord> {

    /**
     * 获取最新一条记录
     *
     * @param memberId 会员ID
     * @param coinUnit 币种
     * @return
     */
    CywWalletSyncRecord getNewest(Long memberId, String coinUnit);

    /**
     * 根据时间段汇总数据
     *
     * @param memberId
     * @param coinUnit
     * @param startTime
     * @param endTime
     * @return
     */
    WalletSyncCountDto sum(Long memberId, String coinUnit, Date startTime, Date endTime);
}