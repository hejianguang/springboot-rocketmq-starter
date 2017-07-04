package com.beescm.controller;

import com.alibaba.rocketmq.client.producer.*;
import com.alibaba.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/5/19.
 */
@RestController
public class ProducerController {

    @Autowired
    @Qualifier("default")
    private DefaultMQProducer defaultMQProducer;


    @Autowired
    @Qualifier("trans")
    private TransactionMQProducer transactionMQProducer;


    @RequestMapping("send_msg")
    public String sendMsg() {
        Message msg = new Message("test",// topic
                "tag1",// tag
                "12345666",// key
                ("这是一个测试消息1").getBytes());// body
        try {
            defaultMQProducer.send(msg,new SendCallback(){

                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println(sendResult);
                    //TODO 发送成功处理
                }

                @Override
                public void onException(Throwable e) {
                    System.out.println(e);
                    //TODO 发送失败处理
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return "exception";
        }
        return "success";
    }


    @RequestMapping(value = "/send_transaction_msg", method = RequestMethod.GET)
    public String sendTransactionMsg() {
        SendResult sendResult = null;
        try {
            //构造消息
            Message msg = new Message("test",// topic
                    "tag2",// tag
                    "test1",// key
                    ("这是一个事物测试消息").getBytes());// body

            //发送事务消息，LocalTransactionExecute的executeLocalTransactionBranch方法中执行本地逻辑
            sendResult = transactionMQProducer.sendMessageInTransaction(msg, new LocalTransactionExecuter() {
                @Override
                public LocalTransactionState executeLocalTransactionBranch(Message message, Object o) {
                    int value = 1;
                    //TODO 执行本地事务，改变value的值
                    //===================================================
                    System.out.println("执行本地事务。。。完成");
                    if(o instanceof Integer){
                        value = (Integer)o;
                    }
                    //===================================================

                    if (value == 0) {
                        throw new RuntimeException("Could not find db");
                    } else if ((value % 5) == 0) {
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    } else if ((value % 4) == 0) {
                        return LocalTransactionState.COMMIT_MESSAGE;
                    }
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }, 4);

        } catch (Exception e) {
            e.printStackTrace();
            return "exception";
        }
        if (StringUtils.isEmpty(sendResult)){
            return "error";
        }
        return sendResult.toString();
    }
}
