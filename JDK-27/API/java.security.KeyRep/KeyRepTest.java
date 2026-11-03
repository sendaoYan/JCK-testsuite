/*
 * @test
 * @summary java.security.KeyRep conformance: white-box structure + black-box behavior.
 * @run main KeyRepTest
 */

import java.lang.reflect.Modifier;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyRep;
import java.util.Arrays;

public class KeyRepTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyRep");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        byte[] pubEncoded = kp.getPublic().getEncoded();
        byte[] privEncoded = kp.getPrivate().getEncoded();

        // PUBLIC type resolves to a PublicKey via X509EncodedKeySpec
        KeyRep pubRep = new KeyRep(KeyRep.Type.PUBLIC, "RSA", "X.509", pubEncoded);
        Object pubObj = roundtrip(pubRep);
        check(pubObj instanceof Key, "PUBLIC readResolve returns a Key");
        check("RSA".equals(((Key) pubObj).getAlgorithm()), "PUBLIC key algorithm RSA");
        check(Arrays.equals(pubEncoded, ((Key) pubObj).getEncoded()), "PUBLIC key encoding preserved");

        // PRIVATE type resolves to a PrivateKey via PKCS8EncodedKeySpec
        KeyRep privRep = new KeyRep(KeyRep.Type.PRIVATE, "RSA", "PKCS#8", privEncoded);
        Object privObj = roundtrip(privRep);
        check(privObj instanceof Key, "PRIVATE readResolve returns a Key");
        check("RSA".equals(((Key) privObj).getAlgorithm()), "PRIVATE key algorithm RSA");
        check(Arrays.equals(privEncoded, ((Key) privObj).getEncoded()), "PRIVATE key encoding preserved");

        // SECRET type resolves via SecretKeySpec
        byte[] raw = {1, 2, 3, 4, 5, 6, 7, 8};
        KeyRep secretRep = new KeyRep(KeyRep.Type.SECRET, "AES", "RAW", raw);
        Object secretObj = roundtrip(secretRep);
        check(secretObj instanceof Key, "SECRET readResolve returns a Key");
        check("AES".equals(((Key) secretObj).getAlgorithm()), "SECRET key algorithm AES");
        check("RAW".equals(((Key) secretObj).getFormat()), "SECRET key format RAW");
        check(Arrays.equals(raw, ((Key) secretObj).getEncoded()), "SECRET key encoding preserved");
    }

    static Object roundtrip(java.io.Serializable obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
