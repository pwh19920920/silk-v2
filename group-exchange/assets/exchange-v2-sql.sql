CREATE TABLE `exchange_wallet` (
  `id` varchar(64) COLLATE utf8_bin NOT NULL COMMENT 'ID, MemberId:CoinUnit',
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '钱包地址',
  `balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '余额',
  `frozen_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '冻结余额',
  `member_id` bigint(20) DEFAULT NULL COMMENT '会员ID',
  `coin_unit` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '币种',
  `is_lock` int(11) DEFAULT '0' COMMENT '是否锁定',
  `signature` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT '签名',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户币币账户';

CREATE TABLE `exchange_wallet_wal_record` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `member_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_unit` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '币种单位',
  `trade_balance` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '变动的可用余额',
  `trade_frozen` decimal(24,8) DEFAULT '0.00000000' COMMENT '变动的冻结余额',
  `fee` decimal(24,8) DEFAULT '0.00000000' COMMENT '交易手续费',
  `fee_discount` decimal(24,8) DEFAULT '0.00000000' COMMENT '优惠手续费',
  `rate` decimal(24,8) DEFAULT '0.00000000' COMMENT '汇率（USDT）',
  `trade_type` int(11) NOT NULL COMMENT '交易类型',
  `ref_id` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '关联的业务ID',
  `sync_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '同步ID',
  `signature` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT '签名',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态：0=未处理，1=已处理',
  `order_match_type` int(11) NOT NULL DEFAULT '2' COMMENT '状态：0=吃单(Taker), 1=挂单(Maker), 2=NULL',
--   `tcc_status` int(11) NOT NULL DEFAULT '0' COMMENT 'tcc状态：0=none，1=try，2=confirm，3=cancel',
--   `trade_status` int(11) NOT NULL DEFAULT '0' COMMENT '交易状态：0=trading, 1=partial, 2=complete, 3=cancel',
  `remark` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='币币账户WAL流水记录表';

CREATE TABLE `exchange_wallet_sync_record` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `wallet_id` varchar(64) NOT NULL COMMENT '账户ID',
  `member_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_unit` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '币种单位',
  `sum_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '总计变动数额',
  `increased_amount` decimal(24,8) NOT NULL DEFAULT '0.00000000' COMMENT '增加的变动数额',
  `sum_frozen_amount` decimal(24,8) DEFAULT '0.00000000' COMMENT '总计冻结数额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `status` int(11) DEFAULT '0' COMMENT '状态：0-进行中，1-已完成',
  `create_time` datetime DEFAULT NULL COMMENT '创建日期',
  `update_time` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='币币账户WAL流水同步记录表';


CREATE TABLE `exchange_wallet_reset_record` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `wallet_id` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '格式=MemberId:CoinUnit',
  `member_id` bigint(20) DEFAULT NULL COMMENT '会员ID',
  `coin_unit` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '币种',
  `balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '重置前的余额',
  `frozen_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '重置前的冻结余额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime NOT NULL COMMENT '创建日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户币币账户重置记录';

-- 机器人订单表
-- CREATE TABLE `exchange_cyw_order` (
--   `order_id` varchar(255) NOT NULL COMMENT '订单号，S开头的订单为星客机器人订单',
--   `member_id` bigint(20) NOT NULL  COMMENT '会员ID',
--   `amount` decimal(18,8) NOT NULL  COMMENT '交易数量',
--   `direction` int(11)  NOT NULL  COMMENT '订单方向 0买，1卖',
--   `price` decimal(18,8) NOT NULL  COMMENT '挂单价格',
--   `symbol` varchar(255) NOT NULL  COMMENT '交易对',
--   `type` int(11) NOT NULL  COMMENT '挂单类型，0市价，1限价',
--
--   `coin_symbol` varchar(255) DEFAULT NULL COMMENT '交易币',
--   `base_symbol` varchar(255) DEFAULT NULL COMMENT '结算币',
--
--   `status` int(11) NOT NULL COMMENT '订单状态 0交易，1完成，2取消，3超时',
--
--   `time` bigint(20) NOT NULL COMMENT '下单时间',
--   `completed_time` bigint(20) DEFAULT NULL COMMENT '交易完成时间',
--   `canceled_time` bigint(20) DEFAULT NULL COMMENT '交易取消时间',
--
--   `validated` int(11) NOT NULL DEFAULT 0 COMMENT '校验状态：0 未校验， 1 校验通过， 2 校验失败',
--
--   `traded_amount` decimal(19,8) DEFAULT '0.00000000' COMMENT '成交量',
--   `turnover` decimal(19,8) DEFAULT '0.00000000' COMMENT '成交额，对市价买单有用',
--   `freeze_amount` decimal(18,8) NOT NULL COMMENT '买入或卖出量 对应的 冻结币数量',
--   PRIMARY KEY (`order_id`),
--   KEY `index_exchange_cyw_order` (`member_id`,`symbol`,`status`) USING BTREE,
--   KEY `complated_time_index` (`completed_time`),
--   KEY `time` (`time`) USING BTREE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


alter table exchange_order
  add column `validated` int(11) NOT NULL DEFAULT 0 COMMENT '校验状态：0 未校验， 1 校验通过， 2 校验失败';

alter table exchange_coin
  add column `trade_captcha` varchar(255) DEFAULT NULL COMMENT '交易验证码';
alter table exchange_coin
  modify column `fee_buy_discount`  decimal(8,4) comment '吃单：买币手续费的折扣率';
alter table exchange_coin
  modify column `fee_sell_discount` decimal(8,4) comment '吃单：卖币手续费的折扣率';
alter table exchange_coin
  add column `fee_entrust_buy_discount`  decimal(8,4) default 0 COMMENT '挂单：买币手续费的折扣率';
alter table exchange_coin
  add column `fee_entrust_sell_discount` decimal(8,4) default 0 COMMENT '挂单：卖币手续费的折扣率';

alter table exchange_coin
  add column `rank_min_trade_times` int(11) default 0 COMMENT '排名：最低交易笔数';
alter table exchange_coin
  add column `rank_min_trade_amount` decimal(18,8) default 0 COMMENT '排名：最低交易成数量';
-- alter table exchange_coin
--   add column `rank_min_trade_turnover` decimal(18,8) default 0 COMMENT '排名：最低交易成交额';



alter table exchange_member_discount_rule
  modify column `fee_buy_discount`  decimal(8,4) comment '吃单：买币手续费的折扣率';
alter table exchange_member_discount_rule
  modify column `fee_sell_discount` decimal(8,4) comment '吃单：卖币手续费的折扣率';
alter table exchange_member_discount_rule
  add column `fee_entrust_buy_discount`  decimal(8,4) default 0 COMMENT '挂单：买币手续费的折扣率';
alter table exchange_member_discount_rule
  add column `fee_entrust_sell_discount` decimal(8,4) default 0 COMMENT '挂单：卖币手续费的折扣率';

alter table exchange_wallet_wal_record
  modify column `order_match_type` int(11) NOT NULL DEFAULT '0' COMMENT '订单撮合类型：0=NULL,1=买入吃单, 2=买入挂单, 3=卖出吃单，4=卖出挂单';

-- 备份数据
create table exchange_coin_bak201911 like exchange_coin;
insert into exchange_coin_bak201911 select * from exchange_coin;

-- 初始化挂单手续费
update exchange_coin set fee_entrust_buy_discount = fee_buy_discount,fee_entrust_sell_discount=fee_sell_discount;


-- 备份数据
create table exchange_member_discount_rule_bak201911 like exchange_member_discount_rule;
insert into exchange_member_discount_rule_bak201911 select * from exchange_member_discount_rule;

-- 初始化挂单手续费
update exchange_member_discount_rule set fee_entrust_buy_discount = fee_buy_discount,fee_entrust_sell_discount=fee_sell_discount;

-- 钱包每日快照表
-- CREATE TABLE `exchange_wallet_snapshoot` (
--     `id` bigint(20) NOT NULL COMMENT 'ID',
--     `op_time` int(11) NOT NULL COMMENT '数据周期',
--     `member_id` bigint(20) DEFAULT NULL COMMENT '会员ID',
--     `coin_unit` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '币种',
--     `prev_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '上一期余额',
--     `prev_frozen_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '上一期冻结余额',
--     `balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '当前余额',
--     `frozen_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '当前冻结余额',
--
--     `sum_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '当日交易余额',
--     `sum_frozen_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '当日交易冻结余额',
--
--     /* 快照时 按市价折算为usdt的汇率，衡量总资产 */
--     `rate` decimal(24,8) DEFAULT '0.00000000' COMMENT '汇率（USDT）',
--
--     `cumsum_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '当日累计增加的交易余额',
--     /*`cumsum_frozen_balance` decimal(24,8) DEFAULT '0.00000000' COMMENT '当日累计增加的冻结余额',*/
--
--     /* 平衡关系：balance + frozen_balance = prev_balance + prev_frozen_balance + sum_balance + sum_frozen_balance */
--     `check_status` int(11) DEFAULT '0' COMMENT '平衡关系状态：0-未知，1-满足平衡关系，2-不满足平衡关系',
--     `snapshoot_time` datetime NOT NULL COMMENT '快照时间',
--     `create_time` datetime DEFAULT NULL COMMENT '创建日期',
--     PRIMARY KEY (`id`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='钱包每日快照表';

-- exchange_wallet_wal_record 索引优化
-- ALTER TABLE exchange_wallet_wal_record ADD INDEX index_exchange_wallet_wal_record_sync_id (sync_id);
ALTER TABLE exchange_wallet_wal_record ADD INDEX index_exchange_wallet_wal_record_smc (sync_id,member_id,coin_unit);
-- ALTER TABLE exchange_wallet_wal_record ADD INDEX index_exchange_wallet_wal_record_tc (trade_type,create_time);
ALTER TABLE exchange_wallet_wal_record ADD INDEX index_exchange_wallet_wal_record_r (ref_id);

-- exchange_wallet_sync_record 索引优化
ALTER TABLE exchange_wallet_sync_record ADD INDEX index_exchange_wallet_sync_record_mcc (member_id,coin_unit,create_time);

-- exchange_wallet_snapshoot 索引优化
-- ALTER TABLE exchange_wallet_snapshoot ADD INDEX index_exchange_wallet_snapshoot_omc (op_time,member_id,coin_unit);

-- exchange_wallet_wal_record 分表
create table exchange_wallet_wal_record_0 like exchange_wallet_wal_record;
create table exchange_wallet_wal_record_1 like exchange_wallet_wal_record;
create table exchange_wallet_wal_record_2 like exchange_wallet_wal_record;
create table exchange_wallet_wal_record_3 like exchange_wallet_wal_record;
create table exchange_wallet_wal_record_4 like exchange_wallet_wal_record;
create table exchange_wallet_wal_record_5 like exchange_wallet_wal_record;
create table exchange_wallet_wal_record_6 like exchange_wallet_wal_record;
create table exchange_wallet_wal_record_7 like exchange_wallet_wal_record;

-- exchange_exchange_order 索引优化

-- exchange_exchange_order 分表
-- create table exchange_exchange_order_0 like exchange_exchange_order;
-- create table exchange_exchange_order_1 like exchange_exchange_order;
-- create table exchange_exchange_order_2 like exchange_exchange_order;
-- create table exchange_exchange_order_3 like exchange_exchange_order;
-- create table exchange_exchange_order_4 like exchange_exchange_order;
-- create table exchange_exchange_order_5 like exchange_exchange_order;
-- create table exchange_exchange_order_6 like exchange_exchange_order;
-- create table exchange_exchange_order_7 like exchange_exchange_order;

-- 创建昨日归档表 todo 修改为上线的前一天
-- create table  exchange_wallet_wal_record_his_20191007 like exchange_wallet_wal_record;
-- create table  exchange_wallet_wal_record_his_20191008 like exchange_wallet_wal_record;
-- create table  exchange_wallet_wal_record_his_20191009 like exchange_wallet_wal_record;
-- create table  exchange_wallet_wal_record_his_20191010 like exchange_wallet_wal_record;