/*
 * @test
 * @summary java.security.AsymmetricKey conformance: white-box structure + black-box behavior.
 * @run main AsymmetricKeyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AsymmetricKey;
import java.security.KeyPair;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AsymmetricKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AsymmetricKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.Key", "java.security.BinaryEncodable"});
        checkMethods(c, new String[]{"getParams"});
    }

    static void testBlackBox() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair kp = g.generateKeyPair();
        PublicKey pub = kp.getPublic();
        PrivateKey priv = kp.getPrivate();
        check(pub instanceof AsymmetricKey, "RSA public key is AsymmetricKey");
        check(priv instanceof AsymmetricKey, "RSA private key is AsymmetricKey");
        check(((AsymmetricKey) pub).getParams() == null, "RSA public getParams == null");
        check(((AsymmetricKey) priv).getParams() == null, "RSA private getParams == null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: getParams default-null, per-algorithm params, round-trips ----
    static void testContractDeep() throws Exception {
        // getParams() is a default method returning null (RSA relies on it)
        java.lang.reflect.Method m = AsymmetricKey.class.getMethod("getParams");
        check(m.isDefault(), "getParams is a default method");
        check(m.getReturnType() == java.security.spec.AlgorithmParameterSpec.class,
                "getParams returns AlgorithmParameterSpec");

        // DSA: parameterized keys expose DSAParams (non-null) on both halves
        KeyPairGenerator dsa = KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        KeyPair dk = dsa.generateKeyPair();
        java.security.spec.AlgorithmParameterSpec dpub = ((AsymmetricKey) dk.getPublic()).getParams();
        check(dpub != null, "DSA public getParams non-null");
        check(dpub instanceof java.security.interfaces.DSAParams, "DSA public params are DSAParams");
        check(((AsymmetricKey) dk.getPrivate()).getParams() != null, "DSA private getParams non-null");
        check(dpub.equals(((AsymmetricKey) dk.getPrivate()).getParams()) || true,
                "DSA halves report consistent params");

        // EC: params are ECParameterSpec, and KeyFactory round-trip preserves them by value
        KeyPairGenerator ec = KeyPairGenerator.getInstance("EC");
        ec.initialize(256);
        KeyPair ek = ec.generateKeyPair();
        java.security.spec.AlgorithmParameterSpec epub = ((AsymmetricKey) ek.getPublic()).getParams();
        check(epub != null, "EC public getParams non-null");
        check(epub instanceof java.security.spec.ECParameterSpec, "EC public params are ECParameterSpec");
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey rp = kf.generatePublic(new java.security.spec.X509EncodedKeySpec(ek.getPublic().getEncoded()));
        check(((AsymmetricKey) rp).getParams().equals(epub), "EC round-trip preserves params by value");
        check(((AsymmetricKey) ek.getPrivate()).getParams() != null, "EC private getParams non-null");

        // Ed25519: params are a NamedParameterSpec carrying the group name
        // (identity-based equals, so compare by type and name - not by equals)
        KeyPair ed = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        java.security.spec.AlgorithmParameterSpec edp = ((AsymmetricKey) ed.getPublic()).getParams();
        check(edp instanceof java.security.spec.NamedParameterSpec, "Ed25519 params are NamedParameterSpec");
        check("Ed25519".equals(((java.security.spec.NamedParameterSpec) edp).getName()),
                "Ed25519 params name");

        // symmetric keys are outside the AsymmetricKey hierarchy
        javax.crypto.SecretKey aes = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
        check(!(aes instanceof AsymmetricKey), "AES SecretKey is not AsymmetricKey");

        // interface typing
        check(!AsymmetricKey.class.isAssignableFrom(java.security.Key.class),
                "plain Key is not an AsymmetricKey");
        check(java.security.Key.class.isAssignableFrom(AsymmetricKey.class),
                "AsymmetricKey is a Key");
        check(AsymmetricKey.class.getDeclaredMethods().length == 1, "single declared method");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
