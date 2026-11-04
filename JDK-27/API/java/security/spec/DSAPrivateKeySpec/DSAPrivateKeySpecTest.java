/*
 * @test
 * @summary java.security.spec.DSAPrivateKeySpec conformance: white-box structure + black-box behavior.
 * @run main DSAPrivateKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.KeySpec;

public class DSAPrivateKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = DSAPrivateKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getX", "getP", "getQ", "getG"});
    }

    static void testBlackBox() {
        DSAPrivateKeySpec spec = new DSAPrivateKeySpec(new BigInteger("1"), new BigInteger("2"), new BigInteger("3"), new BigInteger("4"));
        check(new BigInteger("1").equals(spec.getX()), "getX");
        check(new BigInteger("2").equals(spec.getP()), "getP");
        check(new BigInteger("3").equals(spec.getQ()), "getQ");
        check(new BigInteger("4").equals(spec.getG()), "getG");
        check(spec instanceof KeySpec, "instanceof KeySpec");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：null 语义/引用保持/值语义/KeyFactory 闭环 ----
        java.math.BigInteger dx = java.math.BigInteger.valueOf(11);
        java.math.BigInteger dp = java.math.BigInteger.valueOf(101);
        java.math.BigInteger dq = java.math.BigInteger.valueOf(103);
        java.math.BigInteger dg = java.math.BigInteger.valueOf(107);
        java.security.spec.DSAPrivateKeySpec dks = new java.security.spec.DSAPrivateKeySpec(dx, dp, dq, dg);
        check(dks.getX() == dx, "getX returns the same BigInteger reference");
        check(dks.getP() == dp, "getP returns the same BigInteger reference");
        check(dks.getQ() == dq, "getQ returns the same BigInteger reference");
        check(dks.getG() == dg, "getG returns the same BigInteger reference");
        java.security.spec.DSAPrivateKeySpec dksNull = new java.security.spec.DSAPrivateKeySpec(null, null, null, null);
        check(dksNull.getX() == null, "null-args ctor: getX null");
        check(dksNull.getP() == null, "null-args ctor: getP null");
        check(dksNull.getQ() == null, "null-args ctor: getQ null");
        check(dksNull.getG() == null, "null-args ctor: getG null");
        check(dks instanceof java.security.spec.KeySpec, "implements KeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.DSAPrivateKeySpec.class), "isAssignableFrom KeySpec");
        check(!dks.equals(new java.security.spec.DSAPrivateKeySpec(dx, dp, dq, dg)), "no equals override: identity semantics");
        check(dks.equals(dks), "self-equality");
        check(dks.hashCode() == dks.hashCode(), "hashCode stable");
        check(dks.toString().contains("DSAPrivateKeySpec"), "default toString contains class name");
        check(!(dks instanceof java.io.Serializable), "not Serializable");
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("DSA");
            kpg.initialize(1024);
            java.security.KeyPair kp = kpg.generateKeyPair();
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("DSA");
            java.security.spec.DSAPrivateKeySpec fromKey = kf.getKeySpec(kp.getPrivate(), java.security.spec.DSAPrivateKeySpec.class);
            check(fromKey.getX() != null, "KeyFactory.getKeySpec(DSA private): x non-null");
            check(fromKey.getP() != null, "KeyFactory.getKeySpec(DSA private): p non-null");
            check(fromKey.getQ() != null, "KeyFactory.getKeySpec(DSA private): q non-null");
            check(fromKey.getG() != null, "KeyFactory.getKeySpec(DSA private): g non-null");
            check(fromKey.getP().bitLength() == 1024, "spec p length matches initialized key size");
            java.security.PrivateKey restored = kf.generatePrivate(fromKey);
            check(restored.equals(kp.getPrivate()), "round-trip: spec -> generatePrivate restores the same key");
        } catch (Exception e) {
            throw new AssertionError("KeyFactory interop failed: " + e, e);
        }
    }

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
