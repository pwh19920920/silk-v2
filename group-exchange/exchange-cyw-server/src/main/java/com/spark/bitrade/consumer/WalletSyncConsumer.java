package com.spark.bitrade.consumer;

import com.spark.bitrade.entity.CywWalletSyncRecord;
import com.spark.bitrade.entity.constants.CywConstants;
import com.spark.bitrade.entity.constants.CywRedisKeys;
import com.spark.bitrade.service.CywWalletOperations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 钱包同步通知消费
 * <p>
 * 1.消费Kafka消息，加入redis链表 <br/>
 * 2.周期性从redis链表pop，保证时序性<br/>
 *
 * @author Archx[archx@foxmail.com]
 * @since 2019/9/9 11:15
 */
@Slf4j
@Component
public class WalletSyncConsumer implements InitializingBean, DisposableBean {

    private StringRedisTemplate redisTemplate;
    private ScheduledExecutorService scheduled;
    private CywWalletOperations walletOperations;

    /**
     * 消费订阅通知消息
     * <p>
     * record.value = memberId:coinUnit
     *
     * @param record record
     */
    @KafkaListener(topics = CywConstants.KAFKA_MSG_WALLET_WAL_SYNC_TASK, group = "group-handle")
    public void consume(ConsumerRecord<String, String> record) {

        String value = record.value();
        if (StringUtils.hasText(value) && value.contains(":")) {
            // 加入缓存链表
            redisTemplate.opsForSet().add(CywRedisKeys.CYW_WALLET_SYNC_KEY, value);
        }
    }

    private void handle(String value) {
        // 还原信息
        String[] data = value.replaceAll("\"","").split(":");
        if (data.length != 2) {
            log.error("无效的消息 value = {}", value);
            return;
        }

        long memberId = NumberUtils.toLong(data[0], 0);
        String coinUnit = data[1];

        // 是否有必要异步?????

        try {
            Optional<CywWalletSyncRecord> sync = walletOperations.sync(memberId, coinUnit);
            if (sync.isPresent()) {
                CywWalletSyncRecord record = sync.get();
                log.info("同步成功 member_id = {}, coin_unit = {}, amount = {}, increment = {}, frozen = {}",
                        memberId, coinUnit, record.getSumAmount(), record.getIncreasedAmount(), record.getSumFrozenAmount());
            } else {
                log.error("同步失败 member_id = {}, coin_unit = {}", memberId, coinUnit);
            }
        } catch (Exception ex) {
            log.error("同步失败 member_id = {}, coin_unit = {}", memberId, coinUnit);
            log.error("同步失败 err -> ", ex);
        }

    }

    @Override
    public void destroy() throws Exception {
        if (scheduled != null) {
            scheduled.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduled = Executors.newSingleThreadScheduledExecutor();
        scheduled.scheduleWithFixedDelay(() -> {
            SetOperations<String, String> operations = redisTemplate.opsForSet();

            Long size = operations.size(CywRedisKeys.CYW_WALLET_SYNC_KEY);
            if (size != null && size > 0) {

                // 已处理的
                Set<String> handled = new HashSet<>();

                while (size-- > 0) {
                    String value = operations.pop(CywRedisKeys.CYW_WALLET_SYNC_KEY);
                    // 无待处理消息则中断
                    if (!StringUtils.hasText(value)) {
                        break;
                    }

                    // 已处理过的则跳过
                    if (handled.contains(value)) {
                        continue;
                    }

                    handle(value);

                    // 加入已处理
                    handled.add(value);
                }
            }
        }, 30, 60, TimeUnit.SECONDS);
    }

    // ----------------------------------------
    // SETTERS ...
    // ----------------------------------------

    @Autowired
    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        this.redisTemplate = new StringRedisTemplate(redisConnectionFactory);
    }

    @Autowired
    public void setWalletOperations(CywWalletOperations walletOperations) {
        this.walletOperations = walletOperations;
    }
}
