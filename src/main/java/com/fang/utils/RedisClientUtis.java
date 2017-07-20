package com.fang.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/5/22.
 */
public class RedisClientUtis {
    private static Logger logger = LoggerFactory.getLogger(RedisClientUtis.class);
    private static ExecutorService executor;
    private static JedisPool pool;

    static {
        /*/JedisPoolConfig config = new JedisPoolConfig();
        /连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        config.setBlockWhenExhausted(true);
        //设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        //是否启用pool的jmx管理功能, 默认true
        config.setJmxEnabled(true);
        //MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + "pool" + i); 默 认为"pool", JMX不熟,具体不知道是干啥的...默认就好.
        config.setJmxNamePrefix("pool");
        //是否启用后进先出, 默认true
        config.setLifo(true);
        //最大空闲连接数, 默认8个
        config.setMaxIdle(8);
        //最大连接数, 默认8个
        config.setMaxTotal(8);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(-1);
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTimeMillis(1800000);
        //最小空闲连接数, 默认0
        config.setMinIdle(0);
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(3);
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        config.setSoftMinEvictableIdleTimeMillis(1800000);
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(false);
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(false);
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(-1);
        pool=new JedisPool(config, "124.251.50.24", 6379, 100000);*/

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置最大实例总数
        jedisPoolConfig.setMaxTotal(150);
        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        jedisPoolConfig.setMaxIdle(30);
        jedisPoolConfig.setMinIdle(10);
        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        jedisPoolConfig.setMaxWaitMillis(3 * 1000);
        // 在borrow一个jedis实例时，是否提前进行alidate操作；如果为true，则得到的jedis实例均是可用的；
        jedisPoolConfig.setTestOnBorrow(true);
        // 在还会给pool时，是否提前进行validate操作
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(500);
        jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(1000);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(1000);
        jedisPoolConfig.setNumTestsPerEvictionRun(100);
        pool = new JedisPool(jedisPoolConfig, "124.251.50.24", 6379, 100000);

        executor = Executors.newCachedThreadPool();
    }

    public static JedisPool getPool() {
        return pool;
    }

    public static void setPool(JedisPool pool) {
        RedisClientUtis.pool = pool;
    }

    public static void readRedisByRange(int start, int end) {
        System.out.println("start" + start + " end" + end);
        Jedis jedis = pool.getResource();
        String tokenKeyFormat;
        String tempString;
        int a = start;
        try {
            for (; start < end; start++) {
                tokenKeyFormat = String.format("TOKEN_%d", start);
                tempString = jedis.get(tokenKeyFormat);
                if (Strings.isNullOrEmpty(tempString)) {
                    return;
                }
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        System.out.println(" thread " + Thread.currentThread().getName() + " start:" + a + " end :" + end);
    }

    public static void readRedis() {
        try {
            Integer distributeSize = 100;
            final CountDownLatch cd = new CountDownLatch(distributeSize);
            Integer pageSize = 50;
            for (int i = 1; i <= distributeSize; i++) {
                final int start = (i - 1) * pageSize + 1;
                final int end = i * pageSize;
                executor.execute(new Runnable() {
                    public void run() {
                        readRedisByRange(start, end);
                        cd.countDown();
                    }
                });
            }
            cd.await();
            executor.shutdown();
        } catch (Exception e) {
            //如果缓存连不上，则不处理
            System.out.println("登录无法更新该用户缓存");
        } finally {
            if (pool != null) {
                pool.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CommonUtils.showAllFiles(new File("E:\\p12"), new Processor<File>() {
            @Override
            public void run(File s) {
                if (s.getName().endsWith("p12"))
                {
                    logger.info(s.getName());
                    logger.info(s.getAbsolutePath());
                    String ret = RedisClientUtis.setFile(s);
                    logger.info(ret);
                    //getFile(s.getName(),"E:\\ljjc\\messageCenter\\");
                }
            }
        },false);

    }

    public static boolean  copyFile(File src, File des) {
        boolean ret = true;
        FileInputStream fis = null;
        try {
            FileOutputStream fos = new FileOutputStream(des);
            byte[] b = new byte[1024];
            while (fis.read(b) != -1) {
                fos.write(b);
                fos.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }
    public static void writeRedis() {
        FileReader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader("E:\\ljjc\\tokens.txt");
            br = new BufferedReader(reader);
            Jedis jedis = pool.getResource();
            String tempString = null;
            int line = 1;
            String tokenKeyFormat;
            String str = null;
            while ((str = br.readLine()) != null) {
                tokenKeyFormat = String.format("TOKEN_%d", line);
                System.out.println(str);
                jedis.set(tokenKeyFormat, str);
                line++;
            }
            br.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //如果缓存连不上，则不处理
            System.out.println("登录无法更新该用户缓存");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    //保存文件方法
    public static String setFile(File path) {

        Jedis jedis = pool.getResource();
        byte[] p12file = CommonUtils.file2byte(path.getAbsolutePath());
        return jedis.set(path.getName().getBytes(), p12file);
    }

    public static String setObject(Object k, Object o) {
        Jedis jedis = pool.getResource();
        return jedis.set(CommonUtils.object2Bytes(k), CommonUtils.object2Bytes(o));
    }


    public static Object getObject(Object k) {
        Jedis jedis = pool.getResource();
        return CommonUtils.byte2Object(jedis.get(CommonUtils.object2Bytes(k)));
    }

    //获取文件方法
    public static File getFile(String key,String destDir) {
        Jedis jedis = pool.getResource();
        return CommonUtils.byte2File(jedis.get(key.getBytes()),destDir,key);
    }


    public final static void zadd(String cacheKey,String o)
    {
        Jedis jedis = pool.getResource();
        //Pipeline pipeline = jedis.pipelined();
        long ret = jedis.lpush(cacheKey,o);//.zadd(cacheKey, System.currentTimeMillis(), o)
        jedis.expire(cacheKey, 60*60*48);
    }


}
