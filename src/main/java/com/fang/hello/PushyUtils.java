package com.fang.hello;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fang.utils.CommonUtils;
import com.fang.utils.RedisCacheUtil;
import com.fang.utils.RedisClientUtis;
import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 2017/5/23.
 */
public class PushyUtils {
    private  ApnsClient apnsClient;
    private static Logger logger = LoggerFactory.getLogger(PushyUtils.class);
    private static String p12Dir= CommonUtils.isLinux()?"/logs/ljj/p12/":"E:\\ljjc\\messageCenter\\";
    private static String p12Filename = null;
    public PushyUtils(String product,String os,String isTest)
    {
        p12Filename = "aps_"+product+"_"+os+"_"+isTest+".p12";
        try {
            apnsClient = new ApnsClientBuilder().setClientCredentials(new File(p12Dir+p12Filename), "123456").build();
            //apnsClient = new ApnsClientBuilder().setClientCredentials(new File("E:/p12/aps_"+product+"_"+os+"_"+isTest+".p12"), "123456").build();
            Future<Void> connectFutrue=null;
            if (isTest.equals("1"))
            {
                connectFutrue = apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);
            }else
            {
                connectFutrue = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
            }
            connectFutrue.await(1, TimeUnit.MINUTES);
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof FileNotFoundException)
            {
                RedisClientUtis.getFile(p12Filename,p12Dir);
            }
        } catch (InterruptedException e) {
            logger.info("Failed to connect APNs , timeout");
            e.printStackTrace();
        }
    }
    public PushyUtils()
    {
        this("soufun","iphone","1");
    }

    public static boolean isJson(String string)
    {
        JSONObject js = null;
        try {
            js= JSON.parseObject(string);
        }catch (Exception e)
        {
            js = null;
        }
        return js==null?true:false;
    }

    private static SimpleApnsPushNotification payLoadFactory(String token, String message) {
        ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        JSONObject js = null;
        String payload = null;
        if (isJson(message))
        {
            payloadBuilder.setAlertBody(message);
            payload = payloadBuilder.buildWithDefaultMaximumLength();
        }else
        {
            payload = message;
        }
        String token1 = TokenUtil.sanitizeTokenString(token);
        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token1, "com.soufun.SoufunBasic", payload);
        return pushNotification;
    }

    public static void main(String[] args) throws Exception {
        new PushyUtils().push("42ba73c25d1a923fb693055872407c7303fe7c9ffb275a666b67f9146413e534", "test");
    }

    public  String push(String token, String payload) throws Exception {
        final StringBuilder sb = new StringBuilder();
        SimpleApnsPushNotification notification = payLoadFactory(token, payload);
        Future<PushNotificationResponse<SimpleApnsPushNotification>> responseFuture = apnsClient
                .sendNotification(notification);
        responseFuture
                .addListener(new GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>() {
                    public void operationComplete(Future<PushNotificationResponse<SimpleApnsPushNotification>> arg0)
                            throws Exception {
                        try {
                            final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = arg0
                                    .get();
                            if (pushNotificationResponse.isAccepted()) {
                                logger.info("" + pushNotificationResponse.getPushNotification().getToken() + " push success");
                                sb.append(pushNotificationResponse.getPushNotification().getToken() + " push success");
                            } else {
                                logger.info("" + pushNotificationResponse.getPushNotification().getToken() + " push fail");
                                    logger.info("Notification rejected by the APNs gateway: "
                                            + pushNotificationResponse.getRejectionReason());
                                sb.append("Notification rejected by the APNs gateway: "+ pushNotificationResponse.getRejectionReason());
                                if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                                    logger.info("\t…and the token is invalid as of "
                                            + pushNotificationResponse.getTokenInvalidationTimestamp());

                                }
                            }
                        } catch (final ExecutionException e) {
                            System.err.println("Failed to send push notification.");
                            e.printStackTrace();
                            if (e.getCause() instanceof ClientNotConnectedException) {
                                logger.info("Waiting for client to reconnect…");
                                apnsClient.getReconnectionFuture().await();
                                logger.info("Reconnected.");
                                sb.append("Reconnected");
                                RedisClientUtis.getFile(p12Filename,p12Dir);
                            }
                        }
                    }
                });
        apnsClient.disconnect().sync();
        return sb.toString();
        // 结束后关闭连接, 该操作会直到所有notification都发送完毕并回复状态后关闭连接
/*        Future<Void> disconnectFuture = apnsClient.disconnect();
        try {
            disconnectFuture.await(1, TimeUnit.HOURS);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                logger.info("Failed to disconnect APNs , timeout");
            }
            e.printStackTrace();
        }*/

    }
}
