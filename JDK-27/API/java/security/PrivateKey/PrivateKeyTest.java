/*
 * @test
 * @summary java.security.PrivateKey conformance: white-box structure + black-box behavior.
 * @run main PrivateKeyTest
 */

import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;

public class PrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        checkInterfaces(c, new String[]{"java.security.AsymmetricKey", "javax.security.auth.Destroyable"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PrivateKey priv = kp.getPrivate();

        check(priv instanceof PrivateKey, "instanceof PrivateKey");
        check(priv instanceof java.security.AsymmetricKey, "instanceof AsymmetricKey");
        check(priv instanceof Key, "instanceof Key");
        check(priv instanceof javax.security.auth.Destroyable, "instanceof Destroyable");
        check("RSA".equals(priv.getAlgorithm()), "getAlgorithm RSA");
        check("PKCS#8".equals(priv.getFormat()), "getFormat PKCS#8");
        check(priv.getEncoded() != null && priv.getEncoded().length > 0, "getEncoded non-empty");
        check(((java.security.AsymmetricKey) priv).getParams() == null, "getParams default null");

        // ---- appended contract dimensions ----
        // marker shape: no own methods, fixed serialVersionUID
        check(PrivateKey.class.getDeclaredMethods().length == 0, "no declared methods (marker)");
        check(PrivateKey.class.getDeclaredField("serialVersionUID").getLong(null) == 6034044314589513430L,
                "serialVersionUID 6034044314589513430L");
        check(Key.class.isAssignableFrom(PrivateKey.class), "Key assignable (via AsymmetricKey)");
        check(!java.security.PublicKey.class.isAssignableFrom(PrivateKey.class), "disjoint from PublicKey");
        check(PrivateKey.class.isAssignableFrom(java.security.interfaces.DSAPrivateKey.class),
                "DSAPrivateKey is a PrivateKey");
        check(java.io.Serializable.class.isAssignableFrom(PrivateKey.class), "Serializable");
        // DSA private key: same contract, different algorithm
        KeyPairGenerator dsa = KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        PrivateKey dsaPriv = dsa.generateKeyPair().getPrivate();
        check("DSA".equals(dsaPriv.getAlgorithm()), "DSA algorithm");
        check("PKCS#8".equals(dsaPriv.getFormat()), "DSA PKCS#8");
        check(!dsaPriv.equals(priv), "distinct keys unequal");
        // serialization round-trip: KeyRep path restores an equal key
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(priv);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back instanceof PrivateKey, "round-trip restores PrivateKey");
        check(back.equals(priv), "round-trip equals");
        check(java.util.Arrays.equals(((Key) back).getEncoded(), priv.getEncoded()), "encoding preserved");
        // Destroyable defaults: not destroyed, destroy unsupported by platform keys
        check(!((javax.security.auth.Destroyable) priv).isDestroyed(), "default isDestroyed false");
        try {
            ((javax.security.auth.Destroyable) priv).destroy();
            check(false, "destroy unsupported");
        } catch (javax.security.auth.DestroyFailedException e) {
            check(true, "default destroy throws DestroyFailedException");
        }
        // minimal custom implementation: null format/encoding permitted for opaque keys
        PrivateKey custom = new PrivateKey() {
            private static final long serialVersionUID = 6034044314589513430L;
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
