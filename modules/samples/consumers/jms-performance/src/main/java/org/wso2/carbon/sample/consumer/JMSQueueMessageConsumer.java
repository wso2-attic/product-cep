/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.sample.consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import javax.naming.NamingException;


public class JMSQueueMessageConsumer implements Runnable {
    private static Log log = LogFactory.getLog(JMSQueueMessageConsumer.class);

    private QueueConnectionFactory queueConnectionFactory = null;
    private String queueName = "";
    private boolean active = true;
    private String consumerId;
    private int consumers;
    private int warmUpCount;

    JMSQueueMessageConsumer(String queueName, int consumers, int warmUpCount) {
        this.queueName = queueName;
        this.consumers = consumers;
        this.warmUpCount = warmUpCount;
    }

    public void listen(String consumerId)
            throws InterruptedException, NamingException {

        this.consumerId = consumerId;

        queueConnectionFactory = JNDIContext.getInstance().getQueueConnectionFactory();

        Thread consumerThread = new Thread(this);
        log.info("Starting consumer # " + consumerId);

        consumerThread.start();
    }

    public void shutdown() {
        active = false;
    }

    public void run() {
        // create queue connection
        QueueConnection queueConnection = null;
        try {
            queueConnection = queueConnectionFactory.createQueueConnection();

            queueConnection.start();
        } catch (JMSException e) {
            log.info("Can not create queue connection." + e);
            return;
        }
        Session session = null;
        try {

            session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(destination);

            int count = 0;
            long totalLatency = 0;
            long lastTimestamp = System.currentTimeMillis();

            while (active) {
                Message message = consumer.receive(1000);
                if (message != null) {
//                    if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    long currentTime = System.currentTimeMillis();
                    long sentTimestamp = (Long) mapMessage.getObject("time");

                    totalLatency = totalLatency + (currentTime - sentTimestamp);

                    int logCount = 1000;

                    if ((count % logCount == 0) && (count > warmUpCount)) {
                        double rate = (logCount * 1000.0d / (System.currentTimeMillis() - lastTimestamp));
                        log.info("Consumer: " + consumerId + " (" +
                                logCount + " received) rate: " + rate
                                + " Latency:" + (totalLatency / (logCount * 1.0d)));
//                            log.info("total latency:" + totalLatency);
                        log.info("Total rate: " + (int) (consumers * rate));
                        totalLatency = 0;
                        lastTimestamp = System.currentTimeMillis();
                    }
                    count++;
                }
            }
            log.info("Finished listening for messages.");

        } catch (JMSException e) {
            log.info("Can not subscribe." + e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    log.error(e);
                }
            }
            try {
                queueConnection.stop();
                queueConnection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
