/*
 * @test
 * @summary java.security.BinaryEncodable conformance: white-box structure + black-box behavior.
 *          BinaryEncodable (and PEM/AsymmetricKey) are JDK-27 preview APIs: they cannot
 *          be imported with plain javac (no --enable-preview in this suite), so every
 *          access to them is reflective.
 * @run main BinaryEncodableTest
 */

import java.lang.reflect.Modifier;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class BinaryEncodableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Class<?> BE, PEM_CLASS, AK;
    static void initClasses() throws ClassNotFoundException {
        BE = Class.forName("java.security.BinaryEncodable");
        PEM_CLASS = Class.forName("java.security.PEM");
        AK = Class.forName("java.security.AsymmetricKey");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.BinaryEncodable");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
    }

    static void testBlackBox() throws Exception {
        check(BE.isInstance(new PKCS8EncodedKeySpec(new byte[]{1, 2, 3})), "PKCS8EncodedKeySpec is BinaryEncodable");
        check(BE.isInstance(new X509EncodedKeySpec(new byte[]{1, 2, 3})), "X509EncodedKeySpec is BinaryEncodable");
    }

    public static void main(String[] args) throws Exception {
        initClasses();
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: pure marker surface, spec/key coverage, copy semantics ----
    static void testContractDeep() throws Exception {
        // pure marker: no members, no super-interfaces, not Serializable, not an annotation
        check(BE.getDeclaredMethods().length == 0,
                "pure marker: zero declared methods");
        check(BE.getInterfaces().length == 0,
                "extends no other interface");
        check(!java.io.Serializable.class.isAssignableFrom(BE),
                "not Serializable");
        check(AK.getInterfaces().length == 2
                && Arrays.asList(AK.getInterfaces()).contains(BE),
                "AsymmetricKey extends BinaryEncodable (and Key)");

        // encoded specs: format tags and value/copy semantics
        PKCS8EncodedKeySpec p8 = new PKCS8EncodedKeySpec(new byte[]{1, 2, 3});
        check("PKCS#8".equals(p8.getFormat()), "PKCS8 format tag");
        check(Arrays.equals(p8.getEncoded(), new byte[]{1, 2, 3}), "PKCS8 value");
        check(p8.getEncoded() != p8.getEncoded(), "PKCS8 getEncoded returns a copy each call");
        X509EncodedKeySpec x5 = new X509EncodedKeySpec(new byte[]{4, 5});
        check("X.509".equals(x5.getFormat()), "X509 format tag");
        check(x5.getEncoded() != x5.getEncoded(), "X509 getEncoded returns a copy each call");
        try {
            new PKCS8EncodedKeySpec(null);
            check(false, "null encoded must fail");
        } catch (NullPointerException npe) {
            check(true, "PKCS8EncodedKeySpec(null) -> NPE");
        }
        try {
            new X509EncodedKeySpec(null);
            check(false, "null encoded must fail");
        } catch (NullPointerException npe) {
            check(true, "X509EncodedKeySpec(null) -> NPE");
        }

        // real keys are BinaryEncodable; symmetric keys are not
        java.security.KeyPairGenerator rsa = java.security.KeyPairGenerator.getInstance("RSA");
        rsa.initialize(2048);
        java.security.KeyPair kp = rsa.generateKeyPair();
        check(BE.isInstance(kp.getPublic()), "RSA public key is BinaryEncodable");
        check(BE.isInstance(kp.getPrivate()), "RSA private key is BinaryEncodable");
        check(!BE.isInstance(javax.crypto.KeyGenerator.getInstance("AES").generateKey()),
                "AES SecretKey is not BinaryEncodable");
        // PEM is BinaryEncodable too
        check(BE.isInstance(PEM_CLASS.getConstructor(String.class, String.class)
                .newInstance("TEST", "aGVsbG8=")),
                "PEM is BinaryEncodable");

        // BinaryEncodable is SEALED: user code cannot implement it at all
        check(BE.isSealed(),
                "BinaryEncodable is sealed");
        check(BE.getPermittedSubclasses() != null,
                "sealed with a permits list");
        boolean permitsAsymmetric = false;
        for (Class<?> pc : BE.getPermittedSubclasses())
            if (pc.getName().equals("java.security.AsymmetricKey")) permitsAsymmetric = true;
        check(permitsAsymmetric, "permits includes AsymmetricKey");
    }
}
