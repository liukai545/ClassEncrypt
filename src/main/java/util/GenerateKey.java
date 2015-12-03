package util;

import java.security.*;
import javax.crypto.*;

public class GenerateKey {
    private static String ENCRYPT_ALGORITHM = "DES";

    static public void main(String args[]) throws Exception {
        String keyFilename = args[0];
        // 生成密匙
        SecureRandom sr = new SecureRandom();
        KeyGenerator kg = KeyGenerator.getInstance(ENCRYPT_ALGORITHM);
        kg.init(sr);
        SecretKey key = kg.generateKey();

        // 把密匙数据保存到文件
        FileUtil.writeFile(keyFilename, key.getEncoded());
    }
}

