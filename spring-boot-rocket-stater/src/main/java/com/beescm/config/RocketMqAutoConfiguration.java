package com.beescm.config;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.TransactionMQProducer;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.beescm.event.RocketMqEvent;
import com.beescm.event.TransactionCheckListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/19.
 */
@Configuration
@EnableConfigurationProperties(RocketMqProperties.class)
public class RocketMqAutoConfiguration {


    @Autowired
    private RocketMqProperties rmqProperties;

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 发送普通消息
     */
    @Bean(name = "default")
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(rmqProperties.getProducer());
        producer.setNamesrvAddr(rmqProperties.getNamesrvAddr());
        producer.setInstanceName(rmqProperties.getInstanceName());
        producer.setVipChannelEnabled(false);
        producer.start();
        System.out.println("DefaultMQProducer is Started.");
        return producer;
    }

    /**
     * 发送事务消息
     */
    @Bean(name = "trans")
    public TransactionMQProducer transactionMQProducer() throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer("TransactionProducerGroupName");
        producer.setNamesrvAddr(rmqProperties.getNamesrvAddr());
        producer.setInstanceName(rmqProperties.getInstanceName());
        // 事务回查最小并发数
        producer.setCheckThreadPoolMinSize(2);
        // 事务回查最大并发数
        producer.setCheckThreadPoolMaxSize(2);
        // 队列数
        producer.setCheckRequestHoldMax(2000);
        producer.setTransactionCheckListener(new TransactionCheckListener());
        producer.setVipChannelEnabled(false);
        producer.start();
        System.out.println("TransactionMQProducer is Started.");
        return producer;
    }

    /**
     * 消费者
     */
    @Bean
    public DefaultMQPushConsumer pushConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rmqProperties.getConsumer());
        Set<String> setTopic = rmqProperties.getTopic();
        for (String topic : setTopic) {
            consumer.subscribe(topic, "*");
        }
        consumer.setNamesrvAddr(rmqProperties.getNamesrvAddr());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setConsumeMessageBatchMaxSize(1);
        consumer.setVipChannelEnabled(false);

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                MessageExt msg = list.get(0);
                try {
                    publisher.publishEvent(new RocketMqEvent(consumer, msg));
                } catch (Exception e) {
                    if (msg.getReconsumeTimes() <= 1) {
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        System.out.println("重试失败！");
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);//延迟5秒再启动，主要是等待spring事件监听相关程序初始化完成，否则，回出现对RocketMQ的消息进行消费后立即发布消息到达的事件，然而此事件的监听程序还未初始化，从而造成消息的丢失
                    /**
                     * Consumer对象在使用之前必须要调用start初始化，初始化一次即可<br>
                     */
                    try {
                        consumer.start();
                    } catch (Exception e) {
                        System.out.println("RocketMq pushConsumer Start failure!!!.");
                        e.printStackTrace();
                    }

                    System.out.println("RocketMq pushConsumer Started.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        return consumer;
    }
}
