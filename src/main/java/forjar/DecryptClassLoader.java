package forjar;

import javax.crypto.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Created by liukai on 2015/12/3.
 */
public class DecryptClassLoader extends ClassLoader {
    private static String ENCRYPT_ALGORITHM = "DES";
    private List<Class> classes = new ArrayList<Class>();
    public static Hashtable resources = new Hashtable();

    public DecryptClassLoader(byte[] JarResource, SecretKey key, ClassLoader parent) throws IOException {
        super(parent);
        Cipher cipher = null;
        SecureRandom sr = new SecureRandom();
        try {
            cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, sr);
            init(cipher.doFinal(JarResource));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
    }

    public void init(byte[] resource) throws IOException {

        ArrayList<String> classNames = new ArrayList();
        ArrayList<byte[]> classBuffers = new ArrayList();

        HashMap depends = new HashMap();
        JarInputStream jar = new JarInputStream(new ByteArrayInputStream(resource));
        JarEntry entry;
        while ((entry = jar.getNextJarEntry()) != null) {
            if (entry.getName().toLowerCase().endsWith(".class")) {
                String name = entry.getName().substring(0,
                        entry.getName().length() - ".class".length()).replace('/', '.');
                byte[] data = getResourceData(jar);

                classNames.add(name);
                classBuffers.add(data);
            } else {
                if (entry.getName().charAt(0) == '/') {
                    resources.put(entry.getName(), getResourceData(jar));
                } else {
                    resources.put("/" + entry.getName(), getResourceData(jar));
                }
            }
        }

        while (classNames.size() > 0) {
            int n = classNames.size();
            for (int i = classNames.size() - 1; i >= 0; i--) {
                try {
                    Class clazz = defineClass(classNames.get(i), classBuffers.get(i), 0, classBuffers.get(i).length);
                    classes.add(clazz);
                    System.out.println("--define--" + clazz);
                    String pkName = classNames.get(i);
                    if (pkName.lastIndexOf('.') >= 0) {
                        pkName = pkName.substring(0, pkName.lastIndexOf('.'));
                        if (getPackage(pkName) == null) {
                            definePackage(pkName, null, null, null,
                                    null, null, null, null);
                        }
                    }
                    classNames.remove(i);
                    classBuffers.remove(i);
                } catch (NoClassDefFoundError e) {
                    depends.put((String) classNames.get(i), e.getMessage().replaceAll("/", "."));
                    System.err.println("加载Class出错：" + e.toString());
                } catch (UnsupportedClassVersionError e) {
                    throw new UnsupportedClassVersionError(classNames.get(i)
                            + ", " + java.lang.System.getProperty("java.vm.name") + " "
                            + System.getProperty("java.vm.version") + ")");
                }
            }
            if (n == classNames.size()) {
                for (int i = 0; i < classNames.size(); i++) {
                    System.err.println("NoClassDefFoundError:" + classNames.get(i));
                    String className = (String) classNames.get(i);
                    while (depends.containsKey(className)) {
                        className = (String) depends.get(className);
                    }
                }
                break;
            }
        }
    }

    final static private byte[] getResourceData(JarInputStream jar)
            throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int size;
        while (jar.available() > 0) {
            size = jar.read(buffer);
            if (size > 0) {
                data.write(buffer, 0, size);
            }
        }
        return data.toByteArray();
    }

    @Override
    public URL getResource(String name) {
        if (resources.containsKey("/" + name)) {
            try {
                return new URL("file:///" + name);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return super.getResource(name);
    }
}
