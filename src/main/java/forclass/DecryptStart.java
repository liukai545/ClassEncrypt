package forclass;

import util.FileUtil;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by liukai on 2015/12/3.
 */
public class DecryptStart extends ClassLoader {
    private SecretKey key;
    private Cipher cipher;

    public DecryptStart(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.key = key;
        String algorithm = "DES";
        SecureRandom sr = new SecureRandom();
        cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, sr);
    }

    /**
     * args(0) keyFile
     * args(1) Main Class
     * args(2 +*) Main args
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String keyFileName = args[0];
        String appName = args[1];

        String[] realArgs = new String[args.length - 2];
        System.arraycopy(args, 2, realArgs, 0, args.length - 2);

        //读取密钥
        byte[] rawKey = FileUtil.readFile(keyFileName);
        DESKeySpec dks = new DESKeySpec(rawKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(dks);

        //创建包含解密实现的ClassLoader
        DecryptStart decryptStart = new DecryptStart(key);
        Class clazz = decryptStart.loadClass(appName);

        //拿到main方法
        String proto[] = new String[1];
        Class mainArgs[] = {(new String[1]).getClass()};
        Method main = clazz.getMethod("main", mainArgs);
        Object argsArray[] = {realArgs};
        System.out.println("invoke mian method,start the App");
        main.invoke(null, argsArray);
    }

    @Override
    public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            Class clasz = null;
            // 如果类已经在系统缓冲之中
            clasz = findLoadedClass(name);
            if (clasz != null)
                return clasz;

            try {
                // 读取经过加密的类文件
                byte classData[] = FileUtil.readFile(name + ".class");

                if (classData != null) {
                    byte decryptedClassData[] = cipher.doFinal(classData);
                    clasz = defineClass(name, decryptedClassData,
                            0, decryptedClassData.length);
                }
            } catch (FileNotFoundException fnfe) {
            }

            // 我们尝试用默认的ClassLoader装入它
            if (clasz == null)
                clasz = findSystemClass(name);
            // 如有必要，则装入相关的类
            if (resolve && clasz != null)
                resolveClass(clasz);
            // 把类返回给调用者
            return clasz;
        } catch (IOException ie) {
            throw new ClassNotFoundException(ie.toString()
            );
        } catch (GeneralSecurityException gse) {
            throw new ClassNotFoundException(gse.toString()
            );
        }
    }
}
