package com.fang.hello;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Controller
public class GreetingController {
    private static Logger logger = LoggerFactory.getLogger(GreetingController.class);

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + message.getName() + "!");
    }
    @RequestMapping("/processpush")
    public String newapns()
    {
        return "processpush";
    }
    @RequestMapping("/servlet/newapns")
    public void push(String token ,String product,String message,String isTest,String os, String type ,HttpServletResponse response)
    {
        String res = "";
        long t1 = System.currentTimeMillis();
        logger.info("token:"+token);
        logger.info("product:"+product);
        logger.info("isTest:"+isTest);
        logger.info("os:"+os);
        logger.info("message:"+message);
        if(Strings.isNullOrEmpty(product)) {
            res = "product is null ";
        }

        else if (Strings.isNullOrEmpty(token))  {
            res = "token is null ";
        }
        else if (Strings.isNullOrEmpty(message))  {
            res = "message is null ";
        }
        else if (Strings.isNullOrEmpty(os))  {
            res = "os is null ";
        }

        else if (Strings.isNullOrEmpty(type))  {
            res = "type is null ";

        }
        else if (Strings.isNullOrEmpty(os))  {
            res = "os is null ";
        }
        try {
            PushyUtils push = new PushyUtils(product,os,isTest);
            res = push.push(token,message);
        } catch (Exception e) {
            logger.error("error_2:submit error ",e);
            res = "submit error";
        }finally{
            backResult(res,response);
        }
    }

    public static void backResult(String result,HttpServletResponse response) {

        result = "[{\"result\":\""+result+"\"}]";

        backInfo(result, response);
    }

    public static void backInfo(Object obj ,HttpServletResponse response ) {
        response.setContentType("text/json;charset=utf-8");
        PrintWriter w =null;
        try {
            w = response.getWriter();
            if(obj==null){
                backResult("未查询到相关信息", response);
            }else if(obj instanceof String ){
                w.print(obj);
            }else{
                String s =  JSON.toJSONString(obj);
                w.print(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(w!=null)
                w.close();
        }
    }
}
