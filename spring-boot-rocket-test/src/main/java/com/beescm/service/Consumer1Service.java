package com.beescm.service;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.beescm.event.RocketMqEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/5/19.
 */
@Component
public class Consumer1Service {

    @Async
	@EventListener(condition = "#event.topic=='test'")
	public void testListen(RocketMqEvent event) {
		DefaultMQPushConsumer consumer = event.getConsumer();
		try {
			String msg = new String(event.getMessageExt().getBody(),"utf-8");

			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
			if (event.getMessageExt().getReconsumeTimes() <= 1) {// 重复消费1次
				try {
					consumer.sendMessageBack(event.getMessageExt(), 1);
				} catch (RemotingException | MQBrokerException | InterruptedException | MQClientException e1) {
					e1.printStackTrace();
					//消息进行定时重试
				}
			} else {
				System.out.println("消息消费失败，定时重试");
			}
		}
	}
}
