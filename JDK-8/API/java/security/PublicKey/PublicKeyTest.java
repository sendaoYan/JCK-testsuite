/*
 * @test
 * @summary java.security.PublicKey conformance: interface hierarchy and key methods.
 *          Appended: marker-interface shape (no own methods, serialVersionUID
 *          7187392471159151072L), extends Key (JDK-8; JDK-27 re-parents to AsymmetricKey),
 *          real DSA/RSA public keys instanceof with X.509 format contract,
 *          Serializable round-trip via KeyRep equals closure, hashCode consistency,
 *          PublicKey/PrivateKey disjointness, KeyFactory-produced keys satisfy the interface.
 * @run main PublicKeyTest
 */

public class PublicKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.PublicKey");
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
        Class<?> c = java.security.PublicKey.class;

        // marker interface shape
        check(c.getDeclaredMethods().length == 0, "no declared methods (marker)");
        check(c.getDeclaredField("serialVersionUID").getLong(null) == 7187392471159151072L,
                "serialVersionUID 7187392471159151072L");
        check(java.security.Key.class.isAssignableFrom(c), "extends Key");
        check(!java.security.PrivateKey.class.isAssignableFrom(c), "disjoint from PrivateKey");
        check(c.isAssignableFrom(java.security.interfaces.DSAPublicKey.class),
                "DSAPublicKey is a PublicKey");
        check(c.isAssignableFrom(java.security.interfaces.RSAPublicKey.class),
                "RSAPublicKey is a PublicKey");
        check(java.io.Serializable.class.isAssignableFrom(c), "Keys are Serializable");

        // real RSA public key satisfies the Key contract through PublicKey
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.PublicKey pub = kp.getPublic();
        check(pub instanceof java.security.PublicKey, "RSA public instanceof PublicKey");
        check("RSA".equals(pub.getAlgorithm()), "getAlgorithm RSA");
        check("X.509".equals(pub.getFormat()), "getFormat X.509");
        check(pub.getEncoded() != null && pub.getEncoded().length > 0, "getEncoded non-empty");
        check(!pub.equals(kp.getPrivate()), "public key != private key");

        // KeyFactory-produced key: encoding round-trip keeps PublicKey conformance and equality
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        java.security.spec.X509EncodedKeySpec spec =
                new java.security.spec.X509EncodedKeySpec(pub.getEncoded());
        java.security.PublicKey restored = kf.generatePublic(spec);
        check(restored instanceof java.security.PublicKey, "KeyFactory output instanceof PublicKey");
        check(restored.equals(pub), "restored equals original (cross-instance Key equality)");
        check(restored.hashCode() == pub.hashCode(), "hashCode equal for equal keys");
        check(java.util.Arrays.equals(restored.getEncoded(), pub.getEncoded()), "encoding equal");

        // real DSA public key
        java.security.KeyPairGenerator dsa = java.security.KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        java.security.PublicKey dsaPub = dsa.generateKeyPair().getPublic();
        check(dsaPub instanceof java.security.interfaces.DSAPublicKey, "DSA public is DSAPublicKey");
        check("X.509".equals(dsaPub.getFormat()), "DSA getFormat X.509");

        // serialization round-trip: KeyRep path restores an equal public key
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(pub);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        check(back instanceof java.security.PublicKey, "round-trip restores PublicKey");
        check(back.equals(pub), "round-trip equals original");
        check(((java.security.Key) back).getFormat().equals("X.509"), "format preserved");

        // minimal custom implementation: null format/encoding permitted for opaque keys
        java.security.PublicKey custom = new java.security.PublicKey() {
            private static final long serialVersionUID = 7187392471159151072L;
            public String getAlgorithm() { return "CUSTOM"; }
            public String getFormat() { return null; }
            public byte[] getEncoded() { return null; }
        };
        check("CUSTOM".equals(custom.getAlgorithm()), "custom algorithm");
        check(custom.getFormat() == null && custom.getEncoded() == null,
                "opaque key may return null format/encoding");
        check(!custom.equals(pub), "custom distinct from platform key");
    }
}
