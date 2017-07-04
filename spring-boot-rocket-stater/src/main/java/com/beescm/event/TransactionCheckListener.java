package com.beescm.event;

import com.alibaba.rocketmq.client.producer.LocalTransactionState;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/5/19.
 */
public class TransactionCheckListener implements com.alibaba.rocketmq.client.producer.TransactionCheckListener {

    private AtomicInteger transactionIndex = new AtomicInteger(0);

    @Override
    public LocalTransactionState checkLocalTransactionState(MessageExt messageExt) {
        System.out.println("server checking TrMsg " + messageExt.toString());

        int value = transactionIndex.getAndIncrement();
        if ((value % 6) == 0) {
            throw new RuntimeException("Could not find db");
        } else if ((value % 5) == 0) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        } else if ((value % 4) == 0) {
            return LocalTransactionState.COMMIT_MESSAGE;
        }

        return LocalTransactionState.UNKNOW;
    }
}
