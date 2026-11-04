/*
 * @test
 * @summary java.security.spec.X509EncodedKeySpec conformance: white-box structure + black-box behavior.
 * @run main X509EncodedKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.EncodedKeySpec;
import java.util.Arrays;

public class X509EncodedKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = X509EncodedKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.spec.EncodedKeySpec"), "superclass EncodedKeySpec");
        checkInterfaces(c, new String[]{"java.security.BinaryEncodable"});
        checkMethods(c, new String[]{"getEncoded","getFormat"});
    }

    static void testBlackBox() {
        byte[] key = new byte[]{1,2,3,4};
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        check("X.509".equals(spec.getFormat()), "getFormat");
        byte[] got = spec.getEncoded();
        check(got != key, "getEncoded defensive copy");
        check(Arrays.equals(key, got), "getEncoded content");
        got[0] = 99;
        check(spec.getEncoded()[0] == 1, "getEncoded fresh copy each call");
        key[0] = 88;
        check(spec.getEncoded()[0] == 1, "constructor defensive copy of input");

        check(spec.getAlgorithm() == null, "getAlgorithm null single-arg");
        X509EncodedKeySpec spec2 = new X509EncodedKeySpec(key, "RSA");
        check("RSA".equals(spec2.getAlgorithm()), "getAlgorithm 2-arg");
        check(spec instanceof EncodedKeySpec, "instanceof EncodedKeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：编码字节契约/防御拷贝/值语义/KeyFactory 闭环 ----
        checkThrows(NullPointerException.class, () -> new java.security.spec.X509EncodedKeySpec(null),
                "ctor null bytes -> NPE");
        check(new java.security.spec.X509EncodedKeySpec(new byte[0]).getEncoded().length == 0,
                "empty byte[] legal, getEncoded length 0");
        byte[] rawBytes = {1, 2, 3, 4};
        java.security.spec.X509EncodedKeySpec s1 = new java.security.spec.X509EncodedKeySpec(rawBytes);
        rawBytes[0] = 99;
        check(s1.getEncoded()[0] == 1, "constructor defensive copy");
        byte[] gotA = s1.getEncoded();
        byte[] gotB = s1.getEncoded();
        check(gotA != gotB && java.util.Arrays.equals(gotA, gotB), "getEncoded fresh copy per call, equal content");
        gotA[3] = 0;
        check(s1.getEncoded()[3] == 4, "mutating returned array isolated from spec");
        java.security.spec.X509EncodedKeySpec s2 = new java.security.spec.X509EncodedKeySpec(new byte[]{1, 2, 3, 4});
        check(java.util.Arrays.equals(s1.getEncoded(), s2.getEncoded()), "cross-instance content equality");
        byte[] sharedArr = {5};
        java.security.spec.X509EncodedKeySpec specA = new java.security.spec.X509EncodedKeySpec(sharedArr);
        java.security.spec.X509EncodedKeySpec specB = new java.security.spec.X509EncodedKeySpec(sharedArr);
        specA.getEncoded()[0] = 6;
        check(specB.getEncoded()[0] == 5, "two specs over the same array are fully independent");
        check(!s1.equals(s2), "no equals override: same content, different instances unequal");
        check(s1.hashCode() == s1.hashCode(), "hashCode stable");
        check(s1.toString().contains("X509EncodedKeySpec"), "default toString contains class name");
        check(!(s1 instanceof java.io.Serializable), "not Serializable");
        check(s1 instanceof java.security.spec.EncodedKeySpec && s1 instanceof java.security.spec.KeySpec,
                "instanceof EncodedKeySpec/KeySpec");
        check("X.509".equals(s1.getFormat()), "getFormat exact: X.509");
        check(!s1.equals(new java.security.spec.PKCS8EncodedKeySpec(new byte[]{1, 2, 3, 4})),
                "format identity: X509 and PKCS8 specs never equal");
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512);
            java.security.KeyPair kp = kpg.generateKeyPair();
            byte[] realEnc = kp.getPublic().getEncoded();
            check(realEnc != null && "X.509".equals(kp.getPublic().getFormat()),
                    "real RSA public key uses X.509 format name");
            java.security.spec.X509EncodedKeySpec fromKey = new java.security.spec.X509EncodedKeySpec(realEnc);
            check(java.util.Arrays.equals(realEnc, fromKey.getEncoded()), "wraps real X.509 encoding unchanged");
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            java.security.PublicKey restored = kf.generatePublic(fromKey);
            check(restored.equals(kp.getPublic()), "round-trip: spec -> KeyFactory.generatePublic restores key");
        } catch (Exception e) {
            throw new AssertionError("KeyFactory interop failed: " + e, e);
        }
        // ---- 契约深化（JDK-9+）：算法名构造器 ----
        java.security.spec.X509EncodedKeySpec algSpec = new java.security.spec.X509EncodedKeySpec(new byte[]{7}, "RSA");
        check("RSA".equals(algSpec.getAlgorithm()), "(bytes,alg) ctor: getAlgorithm round-trip");
        check(new java.security.spec.X509EncodedKeySpec(new byte[]{7}).getAlgorithm() == null,
                "1-arg ctor leaves algorithm null");
        checkThrows(NullPointerException.class, () -> new java.security.spec.X509EncodedKeySpec(new byte[]{7}, null),
                "null algorithm -> NPE");
        Class<?> binaryEncodable = null;
        try { binaryEncodable = Class.forName("java.security.BinaryEncodable"); } catch (ClassNotFoundException cnf) { }
        check(binaryEncodable != null && binaryEncodable.isInstance(algSpec),
                "implements java.security.BinaryEncodable (preview marker, checked reflectively)");    }

    // ---- helpers for contract deepening (appended) ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r, String sub, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(sub),
                        msg + ": message contains \"" + sub + "\" (actual: " + t.getMessage() + ")");
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static byte[] serializeToBytes(Object o) {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(o);
            return bos.toByteArray();
        } catch (java.io.IOException e) {
            throw new AssertionError("serialize failed: " + e, e);
        }
    }

    static Object deserializeFromBytes(byte[] b) {
        try {
            return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b)).readObject();
        } catch (Exception e) {
            throw new AssertionError("deserialize failed: " + e, e);
        }
    }
}
