package forjar;

import util.FileUtil;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.lang.reflect.Method;

/**
 * Created by liukai on 2015/12/3.
 */
public class AppStart {
    public static void main(String[] args) throws Exception {
        String keyFileName = args[0];
        String jarFileName = args[1];
        String appName = args[2];

        String[] realArgs = new String[args.length - 3];
        System.arraycopy(args, 3, realArgs, 0, args.length - 3);

        //读取密钥
        byte[] rawKey = FileUtil.readFile(keyFileName);
        DESKeySpec dks = new DESKeySpec(rawKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(dks);

        //创建包含解密实现的ClassLoader
        DecryptClassLoader decryptStart = new DecryptClassLoader(FileUtil.readFile(jarFileName), key, key.getClass().getClassLoader());
        Class clazz = decryptStart.loadClass(appName);

        //拿到main方法
        String proto[] = new String[1];
        Class mainArgs[] = {(new String[1]).getClass()};
        Method main = clazz.getMethod("main", mainArgs);
        Object argsArray[] = {realArgs};
        System.out.println("invoke mian method,start the App");
        main.invoke(null, argsArray);
    }
}
