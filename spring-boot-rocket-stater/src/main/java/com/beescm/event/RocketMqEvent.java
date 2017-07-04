package com.beescm.event;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.springframework.context.ApplicationEvent;

/**
 * Created by Administrator on 2017/5/19.
 */
public class RocketMqEvent extends ApplicationEvent {
    public RocketMqEvent(Object source) {
        super(source);
    }


    private DefaultMQPushConsumer consumer;
    private MessageExt messageExt;
    private String topic;

    public RocketMqEvent(DefaultMQPushConsumer consumer, MessageExt messageExt) {
        super(messageExt);
        this.consumer = consumer;
        this.messageExt = messageExt;
        this.setTopic(messageExt.getTopic());
    }

    public RocketMqEvent(Object source, DefaultMQPushConsumer consumer, MessageExt messageExt, String topic) {
        super(source);
        this.consumer = consumer;
        this.messageExt = messageExt;
        this.topic = topic;
    }


    public DefaultMQPushConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(DefaultMQPushConsumer consumer) {
        this.consumer = consumer;
    }

    public MessageExt getMessageExt() {
        return messageExt;
    }

    public void setMessageExt(MessageExt messageExt) {
        this.messageExt = messageExt;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
