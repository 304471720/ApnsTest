package com.fang.utils;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 2017/5/19.
 */
public class CommonUtils {
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);
    private final static ExecutorService executor;
    // 创建线程池
    private final static ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    static {
        executor = Executors.newCachedThreadPool();
    }

    public static boolean isLinux()
    {
        boolean isLinux = true;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            isLinux = false;
        }
        return  isLinux;
    }
    public static List<String> ExcuteLinuxCommand(String shStr) {
        List<String> strList = new ArrayList<String>(10);
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return strList;
        }
        Process process;
        try {
            process = Runtime.getRuntime().exec(
                    new String[]{"/bin/sh", "-c", shStr}, null, null);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null) {
                strList.add(line);
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return strList;
    }

    public static String ExcuteLinuxCommandRetStr(String command) {
        StringBuilder sb = new StringBuilder();
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            return "";
        }
        Process process;
        try {
            process = Runtime.getRuntime().exec(
                    new String[]{"/bin/sh", "-c", command}, null, null);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            String line;
            process.waitFor();
            while ((line = input.readLine()) != null) {
                sb.append(line);
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getLAN_Ip() {
        String sCmd = "cat /etc/sysconfig/network-scripts/ifcfg-eth1 | grep IPADDR | awk -F '=' '{print $2}'";
        String sLocalIp = "";
        try {
            sLocalIp = ExcuteLinuxCommand(sCmd).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sLocalIp;
    }

    public static String getIpPort(int port) {
        StringBuffer sb = new StringBuffer();
        sb.append(getLAN_Ip()).append(":").append(port);
        return sb.toString();
    }


    public static byte[] object2Bytes(Object value) {
        if (value == null)
            return null;
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(arrayOutputStream);
            outputStream.writeObject(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                arrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayOutputStream.toByteArray();
    }

    public static byte[] file2byte(String filePath)
    {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }

    public static File byte2File(byte[] buf, String filePath, String fileName)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try
        {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory())
            {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static boolean isNumeric(String str) {
        BigDecimal result = null;
        try {
            result = new BigDecimal(str);
        } catch (Exception e) {
        }
        if (result == null)
            return false;
        else
            return true;
    }

    public static Object byte2Object(byte[] bytes) {
        //反序列化方法
        if (bytes == null || bytes.length == 0)
            return null;
        try {
            ObjectInputStream inputStream;
            inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object obj = inputStream.readObject();
            return obj;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Properties readProperties(String filename) {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filename));
            prop.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public static List<String> getMatchList(String pattern, String sourceStr) {
        return regularprocess(pattern, sourceStr, null);
    }

    public static void regularProcessor(String pattern, String sourceStr, Processor t) {
        regularprocess(pattern, sourceStr, t);
    }

    //结尾没有换行符号则表示有处理器，返回处理行数
    //有换行符结尾表示没有处理器，返回文件内容
    public static String lineProcessor(String filename, Processor t) {
        FileReader reader = null;
        BufferedReader br = null;
        Integer line = 0;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new FileReader(filename);
            br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null) {
                if (t != null) {
                    t.run(str);
                } else {
                    sb.append(str).append("\r\n");
                }
                line++;
            }
            if (t != null) {
                sb.append(line);
            }
            br.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    // 正则取某个分组最后匹配的值
    private static List<String> regularprocess(String pattern, String sourceStr, Processor t) {
        Pattern p = Pattern.compile(pattern);
        List<String> ret = new ArrayList<String>();
        Matcher m = p.matcher(sourceStr);
        while (m.find()) {
            if (t != null) {
                t.run(m.group());
            } else {
                ret.add(m.group());
            }
        }
        return ret;
    }

    // 正则取第一次匹配分组的 各个分组值
    public static List<String> getRegGroupContents(String sReg, String s) {
        Pattern p = Pattern.compile(sReg);
        Matcher m = p.matcher(s);
        while (m.find()) {
            List<String> lRet = new ArrayList<String>();
            for (int i = 0; i < m.groupCount(); i++) {
                lRet.add(m.group(i));
            }
            return lRet;
        }
        return null;
    }

    public static void testLib() {
        /*final Jedis jedis = new Jedis("124.251.50.24",6379);
        final Pipeline pipeline = jedis.pipelined();
        final String KeySendUserAllCount="SU_AllCOUNT";
        final AtomicInteger tokenIndex= new AtomicInteger(0);
        final StopWatch  sw = new StopWatch("begin load ..");
        final String pattern = "\\w{64}";
        final ExecutorService es =  Executors.newFixedThreadPool(1);
        lineProcessor("E:\\ljjc\\tokens.txt", new Processor() {
            public void run(Object l) {
                final String tokenlist = (String)l;
                es.execute(new Runnable() {
                    public void run() {
                        regularProcessor(pattern,tokenlist, new Processor() {
                            public void run(Object o) {
                                String token = (String)o;
                                pipeline.set(token.getBytes(),String.valueOf(tokenIndex.get()).getBytes());
                                if (tokenIndex.get()%100000 == 0)
                                {
                                    sw.start(Thread.currentThread().getName()+"begin"+tokenIndex.get());
                                }
                                tokenIndex.getAndIncrement();
                                if (tokenIndex.get()%100000 == 0)
                                {
                                    sw.stop();
                                    System.out.println(sw.prettyPrint());
                                }
                                if (tokenIndex.get()%1000 == 0)
                                {
                                    System.out.println(tokenIndex.get());
                                }
                            }
                        });
                    }
                });
            }
        });
        jedis.set(KeySendUserAllCount.getBytes(),String.valueOf(tokenIndex).getBytes());*/
    }

    public static void main(String[] args) {
        /*String s = ",7b62193b0effdb34295245219964808ea85323955bac6ed08d776eabf60d1201,343697f8fba91f56f909762e507a8d5580e816eebbf966436cb4f2ed206e79ea,eebe97e6d26f091d8eec5b2546af2887a17132afbc0a3e7b76ac7d1c6f205563,71aa010340fe0b1f84a52d98766664af3cd90d32ed8a3e01935872399b3da7e7,faeff500331b4cde7a258bb746ae7c1b37f73cb9e9a20fe96627c139460ab256,34e2adb64e81697aafa6fd35073fdef58b204766cdec987b6718d904597e7c9b,4eb5dc42a4f96688401c53db2659c8791a5c7695ad36b8fa6bee93a37262cb15,f37b73fa4b9f210fed98917b3e67b4c3c675bb2e800a6517b33e26c52898ebbd,71d8627764780a6f47c72997bbf3ee75617660a96241596ef349142bb3cfad87,6ecd82e5559fdad0dcbc12d566e410f67fa3542a86eb9251fcec4940d271ed51,85d83516eea8487cbeb3b8b58245878936a2ac0d5435e1e4f34ba75143372f8a,087a797b270f3540343214305c06c9de1b29dd602009e10eb4ced38291273a27,17791383a05d02a44e2818cbbc17f81cb6e67d7026137f243a8085615da2fbe2,02a2ed63ce8e58a7ab23228e6b3e3ce4193f8d9c3b76e633532c467361224472,179e7482dfb98bea7f93c856b72895d90f135144632e0a584d353fdd1478086a,fa56652e5c5d409464edbaadf50e7dac037200eebff1c6d82bd5762827b9dc13,1cbfab1539f7ba3b4bcab14da01460879749872653cf4cd2a959f149d3006d01,b836131b34d3140247d9b02de534c6587e2df6b94eaf8db5d281e2f07280e9a4,97dda1bac2483f231db58ae56271db147bfb0d91a711a135eead57e02976e9fc,11bc25d4165d46137a9c20c231d3319afe9f0eb9f0acebe9a0bdf565eadd9136,2213226580f543c0e89607ff3bff87057e38c8a21998c743a3283760a7ffb558,a4d61294080f83f87592e03dcbc4d5308058937e7334f2e7b8801690038eaa6d,3cd88196f72ce3defa2a502ddddfaf79d4d28609e8ecc12dafd2f8c500818503,8f064661682ea01437d13fdb48e98973093cc6da727eebac09fb630762641c27,bddb2c262c17ee3faf01c1ce8c319df6ff8d11e3799a92f9fe945dd90b5e97b1,1a75fc5cf01db8aae0db8f2ae93b947c95f7366c4dd971720bb09c6adb5f33c2,8b6c90e9ca6c5f33788e62cbbe1803c09e74214937e6894e49b4819b63c8ea8f,d468c19bcbf02f5c9437d1353be3c1e1739c4f1b11ea102dbe005784426a3ec0,dc65ddd958b21794c7047d4adceb98a41143b6ebbeeb62e80f5278affb203237,829797d3979fc4e6d71bf5d0a33443f099d0aca5956420853371988bcb6357fa,78479baef9f420c607fde9999771635f88fdb9ba5a80a6b5c37f05f52b71aaf6,e79dd1fb4afef1a6ad20a80c598bc0d89a476ed3bd1fd568a75187d7c4a5f62d,d73ca43dc587e85f8aa7a4f36598bfd70b09ea54642c2bd9abfee28eeb8aa5f2,ccf2df4e0e8c8759a56aad6a00896e5f11dc1c7f5bc16ff75be4f7d2a14ecfc8,f224a5c3d9e96c072d3f594e3279c10775466754a04bfa76c4566238b90342fd,d05c997c74f62163045a833ce641dcea918d6e059835e953eac631a4a9d9e894,ec44825b56c43981842ba6309b50a51e4e1f44841aacb7227bd3a704e0cda141,61964d065d5626b643fa04743199c784c28322567736b0edf7a0b73d9ace436d,8c3d04df91c7ff450ef6def8a41932f96b54c8c76dc9cdc95513f2539e296666,866e77f1bbb5ae39228b63d993fa6a6cd61deb3c50d511a316f2d2b4ea205305,6e323444640fcd453bb5971d5b670e724e6f4e259434275046f354c0229fc19c,41e32efbc236fa70cb3121975e6087834978807b3428b3852a61cdc7058ffce8,255f1028ecd99accb65d21bf7075a155e1423261a3da503da922dac83d08675b,0a187c1175a7f5478d33f528550a7d7a97c8dcadeba774d5f79d78dcb08d094f,cc931361b30a29637fa83eb4114762037d1ecdb643678c08e715f31ea7e2d29a";
        String pattern = "\\w{64}";
        List<String> k = regularProcess(pattern,s);
        for (String t : k)
        {
            System.out.println(t);
        }
        regularProcessor(pattern, s, new Processor() {
            public void run(Object o) {
                System.out.println((String)o);
            }
        });*/
        //final Jedis jedis = RedisClientUtis.getPool().getResource();
 /*       final Jedis jedis = new Jedis("124.251.50.24", 6379);
        final Pipeline pipeline = jedis.pipelined();
        final String KeySendUserAllCount = "SU_AllCOUNT";
        final AtomicInteger tokenIndex = new AtomicInteger(0);
        final StopWatch sw = new StopWatch("begin load ..");
        final String pattern = "\\w{64}";
        final ExecutorService es = Executors.newFixedThreadPool(1);
        lineProcessor("E:\\ljjc\\tokens.txt", new Processor() {
            public void run(Object l) {
                final String tokenlist = (String) l;
                es.execute(new Runnable() {
                    public void run() {
                        regularProcessor(pattern, tokenlist, new Processor() {
                            public void run(Object o) {
                                String token = (String) o;
                                if (jedis.exists(token)) {

                                    if (tokenIndex.get() % 100000 == 0) {
                                        sw.start(Thread.currentThread().getName() + "begin" + tokenIndex.get());
                                    }
                                    tokenIndex.getAndIncrement();
                                    if (tokenIndex.get() % 100000 == 0) {
                                        sw.stop();
                                        System.out.println(sw.prettyPrint());
                                    }
                                    if (tokenIndex.get() % 1000 == 0) {
                                        System.out.println(tokenIndex.get());
                                        System.out.println(jedis.get(token));
                                    }
                                } else {
                                    System.out.println(token);
                                }
                            }
                        });
                    }
                });
            }
        });
        jedis.set(KeySendUserAllCount.getBytes(), String.valueOf(tokenIndex).getBytes());
        System.out.println(sw.prettyPrint());*/
        final Jedis jedis = new Jedis("124.251.50.24", 6379);
        final Pipeline pipeline = jedis.pipelined();
        final String KeySendUserAllCount = "SU_AllCOUNT";
        final AtomicInteger tokenIndex = new AtomicInteger(0);
        final StopWatch sw = new StopWatch("begin load ..");
        sw.start();
        final String pattern = "\\w{64}";
        final ExecutorService es = Executors.newFixedThreadPool(1);
        lineProcessor("E:\\ljjc\\tokens.txt", new Processor() {
            public void run(Object l) {
                final String tokenlist = (String) l;
                es.execute(new Runnable() {
                    public void run() {
                        regularProcessor(pattern, tokenlist, new Processor() {
                            public void run(Object o) {
                                String token = (String) o;
                                //pipeline.exists(token);
                                pipeline.get(token);
                            }
                        });
                    }
                });
            }
        });
        //pipeline.set(KeySendUserAllCount.getBytes(), String.valueOf(tokenIndex).getBytes());
        sw.stop();
        System.out.println(sw.prettyPrint());
        sw.start("pipeline syncAndReturnAll");
        List<Object> results = new ArrayList<Object>();
        try {
            results = pipeline.syncAndReturnAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (Object k : results) {
                if (k == null) {
                    continue;
                }
                System.out.println("Item : " + (String) k);
            }
            sw.stop();
            System.out.println("Pipelined GET: " + sw.prettyPrint());
            jedis.close();
            executor.shutdown();
            es.shutdown();
        }
    }


    public final static void  showAllFiles(File dir,Processor<File> p,boolean isRecursively) throws Exception {
        File[] fs = dir.listFiles();
        for (int i = 0; i < fs.length; i++) {
            //logger.info(fs[i].getAbsolutePath());
            if (p != null)
            {
                p.run(fs[i]);
            }
            if (isRecursively)
            {
                if (fs[i].isDirectory()) {
                    try {
                        showAllFiles(fs[i],p,isRecursively);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}
