package util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * Created by liukai on 2015/12/3.
 */
public class EncryptFiles {
    private static String ENCRYPT_ALGORITHM = "DES";

    static public void main(String args[]) throws Exception {
        String keyFilename = args[0];

        // 生成密匙
        SecureRandom sr = new SecureRandom();
        byte rawKey[] = FileUtil.readFile(keyFilename);
        DESKeySpec dks = new DESKeySpec(rawKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPT_ALGORITHM);
        SecretKey key = keyFactory.generateSecret(dks);

        // 创建用于实际加密操作的Cipher对象
        Cipher ecipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        ecipher.init(Cipher.ENCRYPT_MODE, key, sr);

        // 加密命令行中指定的每一个类
        for (int i = 1; i < args.length; ++i) {
            String filename = args[i];
            // 读入类文件
            byte bytes[] = FileUtil.readFile(filename);
            // 加密
            byte encryptedData[] = ecipher.doFinal(bytes);
            // 保存加密后的内容
            FileUtil.writeFile(filename, encryptedData);
            System.out.println("Encrypted " + filename);
        }
    }
}
