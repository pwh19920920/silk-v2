package com.spark.bitrade.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spark.bitrade.api.client.TransferClient;
import com.spark.bitrade.api.vo.ExchangeWalletVo;
import com.spark.bitrade.api.vo.PageResultVo;
import com.spark.bitrade.api.vo.TransferDirectVo;
import com.spark.bitrade.api.vo.WalletQueryVo;
import com.spark.bitrade.constants.CommonMsgCode;
import com.spark.bitrade.constants.ExchangeOrderMsgCode;
import com.spark.bitrade.controller.ApiController;
import com.spark.bitrade.dto.FeeStats;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeWallet;
import com.spark.bitrade.entity.ExchangeWalletWalRecord;
import com.spark.bitrade.entity.constants.ExchangeLockStatus;
import com.spark.bitrade.entity.constants.ExchangeProcessStatus;
import com.spark.bitrade.entity.constants.WalTradeType;
import com.spark.bitrade.service.ExchangeWalletService;
import com.spark.bitrade.service.ExchangeWalletWalRecordService;
import com.spark.bitrade.service.OrderFacadeService;
import com.spark.bitrade.util.AssertUtil;
import com.spark.bitrade.util.BigDecimalUtil;
import com.spark.bitrade.util.MessageRespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 内部接口，专供后台
 *
 * @author Archx[archx@foxmail.com]
 * @since 2019/9/10 14:41
 */
@RestController
@RequestMapping("/internal/v2")
public class Internal2Controller extends ApiController {

    private ExchangeWalletService walletService;
    private ExchangeWalletWalRecordService walRecordService;
    private TransferClient transferClient;
    private OrderFacadeService orderService;

    /**
     * 查看钱包列表
     *
     * @param pageNo   页码
     * @param size     每页显示数量
     * @param memberId 会员id
     * @return list
     */
    @GetMapping("/wallets")
    public MessageRespResult<PageResultVo<ExchangeWallet>> wallet(@RequestParam("page") int pageNo, @RequestParam("size") int size,
                                                                  @RequestParam("memberId") Long memberId) {
        QueryWrapper<ExchangeWallet> query = new QueryWrapper<>();
        if (memberId != null) {
            query.eq("member_id", memberId);
        }
        IPage<ExchangeWallet> page = walletService.page(new Page<>(pageNo, size), query);

        return success(toPageResultVo(pageNo, size, page));
    }

    /**
     * 按条件查询币币账户
     *
     * @param vo
     * @return
     */
    @PostMapping("/walletParam")
    public MessageRespResult<PageResultVo<ExchangeWalletVo>> walletParam(@RequestBody WalletQueryVo vo) {
        IPage<ExchangeWalletVo> page = walletService.findList(vo);
        return success(toPageResultVo(vo.getPage(), vo.getRows(), page));
    }


    /**
     * 创建钱包
     * url： /exchange2/internal/v2/create/wallet?memberId=&coinUnit=
     *
     * @param memberId 会员id
     * @param coinUnit 币种
     * @return wallet
     */
    @RequestMapping(value = "/create/wallet", method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<ExchangeWallet> create(@RequestParam("memberId") Long memberId,
                                                    @RequestParam("coinUnit") String coinUnit) {
        Optional<ExchangeWallet> optional = walletService.findOne(memberId, coinUnit);

        if (optional.isPresent()) {
            return success(optional.get());
        }

        if (walletService.create(memberId, coinUnit)) {
            return success(walletService.getById(memberId + ":" + coinUnit));
        }
        return failed(CommonMsgCode.FAILURE);
    }

    /**
     * 查看资金流水列表
     *
     * @param pageNo   页码
     * @param size     每页显示数量
     * @param memberId 会员id
     * @param coinUnit 币种
     * @param type     交易类型
     * @param status   流水状态
     * @return list
     */
    @GetMapping("/wal/records")
    public MessageRespResult<PageResultVo<ExchangeWalletWalRecord>> wal(@RequestParam("page") int pageNo, @RequestParam("size") int size,
                                                                        @RequestParam(value = "memberId", required = false) Long memberId,
                                                                        @RequestParam(value = "coinUnit", required = false) String coinUnit,
                                                                        @RequestParam(value = "type", required = false) WalTradeType type,
                                                                        @RequestParam(value = "status", required = false) ExchangeProcessStatus status,
                                                                        @RequestParam(value = "startTime", required = false) String startTime,
                                                                        @RequestParam(value = "endTime", required = false) String endTime) {
        QueryWrapper<ExchangeWalletWalRecord> query = new QueryWrapper<>();
        query.lambda().eq(Objects.nonNull(memberId), ExchangeWalletWalRecord::getMemberId, memberId);
        query.lambda().eq(Objects.nonNull(coinUnit), ExchangeWalletWalRecord::getCoinUnit, coinUnit);
        query.lambda().eq(Objects.nonNull(type), ExchangeWalletWalRecord::getTradeType, type);
        query.lambda().eq(Objects.nonNull(status), ExchangeWalletWalRecord::getStatus, status);
        query.lambda().ge(Objects.nonNull(startTime), ExchangeWalletWalRecord::getCreateTime, startTime);
        query.lambda().le(Objects.nonNull(endTime), ExchangeWalletWalRecord::getCreateTime, endTime);

        query.lambda().orderByDesc(ExchangeWalletWalRecord::getCreateTime);

        IPage<ExchangeWalletWalRecord> page = walRecordService.page(new Page<>(pageNo, size), query);

        return success(toPageResultVo(pageNo, size, page));
    }

    /**
     * 转账接口
     * url： /exchange2/internal/v2/transfer?memberId=&coinUnit=&amount=&direction=
     *
     * @param memberId  会员ID
     * @param coinUnit  币种
     * @param amount    数量
     * @param direction 方向
     * @return record
     */
    @RequestMapping(value = "/transfer", method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<ExchangeWalletWalRecord> transfer(@RequestParam("memberId") Long memberId,
                                                               @RequestParam("coinUnit") String coinUnit,
                                                               @RequestParam("amount") BigDecimal amount,
                                                               @RequestParam("direction") TransferDirectVo direction) {
        // 必须时正数
        AssertUtil.isTrue(BigDecimalUtil.gt0(amount), ExchangeOrderMsgCode.ILLEGAL_TRANS_AMOUNT);

        return success(transferClient.transfer(memberId, coinUnit, amount, direction));
    }

    /**
     * 更新钱包锁定状态
     *
     * @param memberId 会员ID
     * @param coinUnit 币种
     * @param lock     锁定状态
     * @return resp
     */
    @RequestMapping(value = "/lock", method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<ExchangeWallet> lock(@RequestParam("memberId") Long memberId,
                                                  @RequestParam("coinUnit") String coinUnit,
                                                  @RequestParam("lock") ExchangeLockStatus lock) {
        Optional<ExchangeWallet> optional = walletService.findOne(memberId, coinUnit);
        if (optional.isPresent()) {
            UpdateWrapper<ExchangeWallet> update = new UpdateWrapper<>();
            update.set("is_lock", lock).eq("id", memberId + ":" + coinUnit);
            walletService.update(update);
            return success(optional.get());
        }
        return failed();
    }

    /**
     * 查看历史订单
     *
     * @param pageNo   页码
     * @param size     每页显示条数
     * @param memberId 会员id
     * @param symbol   交易对
     * @return orders
     * @author zhangYanjun
     * @since 2019.09.19 10:26
     */
    @GetMapping("/historyOrders")
    public MessageRespResult<PageResultVo<ExchangeOrder>> historyOrders(@RequestParam("page") int pageNo, @RequestParam("size") int size,
                                                                        @RequestParam(value = "memberId") Long memberId,
                                                                        @RequestParam(value = "symbol") String symbol) {
        IPage<ExchangeOrder> page = orderService.historyOrders(symbol, memberId, size, pageNo);
        return success(toPageResultVo(pageNo, size, page));
    }

    /**
     * 手续费统计
     * url： /exchange2/internal/v2/feeStats?startTime=2019-11-24 00:00:00&endTime=2019-11-25 00:00:00
     *
     * @param startTime 开始时间（包含），格式=YYYY-MM-DD hh:mm:ss
     * @param endTime   截止时间（不包含），格式=YYYY-MM-DD hh:mm:ss
     * @return
     */
    @RequestMapping(value = "/feeStats", method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<List<FeeStats>> feeStats(@RequestParam("startTime") String startTime,
                                                      @RequestParam("endTime") String endTime) {
        return this.success(walRecordService.stats(startTime, endTime));
    }


    /**
     * 转换为后台接收格式
     *
     * @param pageNo 页码
     * @param size   每页显示数量
     * @param page   查询结果
     * @param <T>    泛型
     * @return vo
     */
    private <T> PageResultVo<T> toPageResultVo(int pageNo, int size, IPage<T> page) {
        return PageResultVo.<T>builder()
                .total(page.getTotal())
                .rows(page.getRecords())
                .hasNext(page.getCurrent() < page.getPages())
                .pageNo(pageNo)
                .pageSize(size).build();
    }


    @Autowired
    public void setWalletService(ExchangeWalletService walletService) {
        this.walletService = walletService;
    }

    @Autowired
    public void setWalRecordService(ExchangeWalletWalRecordService walRecordService) {
        this.walRecordService = walRecordService;
    }

    @Autowired
    public void setTransferClient(TransferClient transferClient) {
        this.transferClient = transferClient;
    }

    @Autowired
    public void setOrderService(OrderFacadeService orderService) {
        this.orderService = orderService;
    }
}
