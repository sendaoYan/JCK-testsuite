/*
 * @test
 * @summary java.security.PrivateKey conformance: interface hierarchy and key methods.
 *          Appended: marker-interface shape (no own methods, serialVersionUID
 *          6034044314589513430L), extends Key + javax.security.auth.Destroyable,
 *          real DSA/RSA private keys instanceof with algorithm/format/encoding contract,
 *          Serializable round-trip via KeyRep equals closure, Destroyable defaults
 *          (destroyed()==false, destroy() throws DestroyFailedException), PublicKey
 *          disjointness, DSAPrivateKey sub-interface membership.
 * @run main PrivateKeyTest
 */

public class PrivateKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.PrivateKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        Class<?> c = java.security.PrivateKey.class;

        // marker interface: declares nothing itself, carries serialVersionUID
        check(c.getDeclaredMethods().length == 0, "no declared methods (marker)");
        check(c.getDeclaredField("serialVersionUID").getLong(null) == 6034044314589513430L,
                "serialVersionUID 6034044314589513430L");
        check(java.security.Key.class.isAssignableFrom(c), "extends Key");
        check(javax.security.auth.Destroyable.class.isAssignableFrom(c), "extends Destroyable");
        check(!java.security.PublicKey.class.isAssignableFrom(c), "disjoint from PublicKey");
        check(c.isAssignableFrom(java.security.interfaces.DSAPrivateKey.class),
                "DSAPrivateKey is a PrivateKey");
        check(c.isAssignableFrom(java.security.interfaces.RSAPrivateKey.class),
                "RSAPrivateKey is a PrivateKey");
        check(java.io.Serializable.class.isAssignableFrom(c), "Keys are Serializable");

        // real RSA private key satisfies the Key contract through PrivateKey
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.PrivateKey priv = kp.getPrivate();
        check(priv instanceof java.security.PrivateKey, "RSA private instanceof PrivateKey");
        check("RSA".equals(priv.getAlgorithm()), "getAlgorithm RSA");
        check("PKCS#8".equals(priv.getFormat()), "getFormat PKCS#8");
        check(priv.getEncoded() != null && priv.getEncoded().length > 0, "getEncoded non-empty");
        check(!priv.equals(kp.getPublic()), "private key != public key");

        // real DSA private key
        java.security.KeyPairGenerator dsa = java.security.KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        java.security.PrivateKey dsaPriv = dsa.generateKeyPair().getPrivate();
        check(dsaPriv instanceof java.security.interfaces.DSAPrivateKey, "DSA private is DSAPrivateKey");
        check("DSA".equals(dsaPriv.getAlgorithm()), "getAlgorithm DSA");
        check("PKCS#8".equals(dsaPriv.getFormat()), "DSA getFormat PKCS#8");

        // serialization round-trip: KeyRep path restores an equal private key
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(priv);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back instanceof java.security.PrivateKey, "round-trip restores PrivateKey");
        check(back.equals(priv), "round-trip equals original");
        check(((java.security.Key) back).getAlgorithm().equals("RSA"), "algorithm preserved");
        check(java.util.Arrays.equals(((java.security.Key) back).getEncoded(), priv.getEncoded()),
                "encoding preserved");

        // Destroyable defaults: not destroyed, destroy unsupported for PII-style keys
        check(priv instanceof javax.security.auth.Destroyable, "Destroyable");
        check(!((javax.security.auth.Destroyable) priv).isDestroyed(), "default isDestroyed()==false");
        try {
            ((javax.security.auth.Destroyable) priv).destroy();
            check(false, "RSA key destroy unsupported");
        } catch (javax.security.auth.DestroyFailedException e) {
            check(true, "default destroy() throws DestroyFailedException");
        }

        // minimal custom implementation: interface imposes no behavior beyond Key
        java.security.PrivateKey custom = new java.security.PrivateKey() {
            private static final long serialVersionUID = 6034044314589513430L;
            public String getAlgorithm() { return "CUSTOM"; }
            public String getFormat() { return null; }
            public byte[] getEncoded() { return null; }
        };
        check("CUSTOM".equals(custom.getAlgorithm()), "custom algorithm");
        check(custom.getFormat() == null && custom.getEncoded() == null,
                "opaque key may return null format/encoding");
    }
}
