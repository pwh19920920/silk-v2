package com.spark.bitrade.entity.chat;

import lombok.Data;
import lombok.ToString;

/**
 * 客户端发送实时消息时的参考规范
 */
@Data
@ToString(callSuper = true)
public class RealTimeChatMessage extends BaseMessage {
    //消息内容
    private String content;
    //消息发送方头像
    private String avatar;
}