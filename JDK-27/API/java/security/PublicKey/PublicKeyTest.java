/*
 * @test
 * @summary java.security.PublicKey conformance: white-box structure + black-box behavior.
 * @run main PublicKeyTest
 */

import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

public class PublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        checkInterfaces(c, new String[]{"java.security.AsymmetricKey"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey pub = kp.getPublic();

        check(pub instanceof PublicKey, "instanceof PublicKey");
        check(pub instanceof java.security.AsymmetricKey, "instanceof AsymmetricKey");
        check(pub instanceof Key, "instanceof Key");
        check("RSA".equals(pub.getAlgorithm()), "getAlgorithm RSA");
        check("X.509".equals(pub.getFormat()), "getFormat X.509");
        check(pub.getEncoded() != null && pub.getEncoded().length > 0, "getEncoded non-empty");

        // AsymmetricKey.getParams() default returns null
        check(((java.security.AsymmetricKey) pub).getParams() == null, "getParams default null");

        // ---- appended contract dimensions ----
        // marker shape: no own methods, fixed serialVersionUID
        check(PublicKey.class.getDeclaredMethods().length == 0, "no declared methods (marker)");
        check(PublicKey.class.getDeclaredField("serialVersionUID").getLong(null) == 7187392471159151072L,
                "serialVersionUID 7187392471159151072L");
        check(Key.class.isAssignableFrom(PublicKey.class), "Key assignable (via AsymmetricKey)");
        check(!java.security.PrivateKey.class.isAssignableFrom(PublicKey.class), "disjoint from PrivateKey");
        check(PublicKey.class.isAssignableFrom(java.security.interfaces.DSAPublicKey.class),
                "DSAPublicKey is a PublicKey");
        check(java.io.Serializable.class.isAssignableFrom(PublicKey.class), "Serializable");
        // KeyFactory encoding round-trip: equal public key with same X.509 material
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey restored = kf.generatePublic(new java.security.spec.X509EncodedKeySpec(pub.getEncoded()));
        check(restored instanceof PublicKey, "KeyFactory output PublicKey");
        check(restored.equals(pub), "restored equals original");
        check(restored.hashCode() == pub.hashCode(), "hashCode equal");
        check(java.util.Arrays.equals(restored.getEncoded(), pub.getEncoded()), "encoding equal");
        // DSA public key: X.509 as well
        KeyPairGenerator dsa = KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        PublicKey dsaPub = dsa.generateKeyPair().getPublic();
        check("X.509".equals(dsaPub.getFormat()), "DSA X.509");
        check(!dsaPub.equals(pub), "distinct keys unequal");
        // serialization round-trip via KeyRep
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(pub);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back instanceof PublicKey, "round-trip restores PublicKey");
        check(back.equals(pub), "round-trip equals");
        // minimal custom implementation
        PublicKey custom = new PublicKey() {
            private static final long serialVersionUID = 7187392471159151072L;
            public String getAlgorithm() { return "CUSTOM"; }
            public String getFormat() { return null; }
            public byte[] getEncoded() { return null; }
        };
        check("CUSTOM".equals(custom.getAlgorithm()), "custom algorithm");
        check(custom.getFormat() == null && custom.getEncoded() == null, "opaque key null format/encoding");
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
