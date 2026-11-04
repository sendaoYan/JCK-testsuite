/*
 * @test
 * @summary java.security.spec.DSAParameterSpec conformance: white-box structure + black-box behavior.
 * @run main DSAParameterSpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;

public class DSAParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = DSAParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec", "java.security.interfaces.DSAParams"});
        checkMethods(c, new String[]{"getP", "getQ", "getG"});
    }

    static void testBlackBox() {
        BigInteger p = new BigInteger("23"), q = new BigInteger("11"), g = new BigInteger("5");
        DSAParameterSpec spec = new DSAParameterSpec(p, q, g);
        check(p.equals(spec.getP()), "getP");
        check(q.equals(spec.getQ()), "getQ");
        check(g.equals(spec.getG()), "getG");
        check(spec instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
        check(spec instanceof DSAParams, "instanceof DSAParams");
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
        // ---- 契约深化：null 语义/引用保持/值语义/AlgorithmParameters 闭环 ----
        java.math.BigInteger p2 = java.math.BigInteger.valueOf(101);
        java.math.BigInteger q2 = java.math.BigInteger.valueOf(103);
        java.math.BigInteger g2 = java.math.BigInteger.valueOf(107);
        java.security.spec.DSAParameterSpec dp2 = new java.security.spec.DSAParameterSpec(p2, q2, g2);
        check(dp2.getP() == p2, "getP returns the same BigInteger reference");
        check(dp2.getQ() == q2, "getQ returns the same BigInteger reference");
        check(dp2.getG() == g2, "getG returns the same BigInteger reference");
        java.security.spec.DSAParameterSpec dpNull = new java.security.spec.DSAParameterSpec(null, null, null);
        check(dpNull.getP() == null, "null-args ctor: getP null");
        check(dpNull.getQ() == null, "null-args ctor: getQ null");
        check(dpNull.getG() == null, "null-args ctor: getG null");
        check(java.security.spec.DSAParameterSpec.class.isAssignableFrom(java.security.spec.DSAParameterSpec.class)
                && !java.security.spec.DSAParameterSpec.class.isAssignableFrom(java.math.BigInteger.class), "isAssignableFrom sanity");
        check(!dp2.equals(new java.security.spec.DSAParameterSpec(p2, q2, g2)), "no equals override: identity semantics");
        check(dp2.equals(dp2), "self-equality");
        check(dp2.hashCode() == dp2.hashCode(), "hashCode stable");
        check(dp2.toString().contains("DSAParameterSpec"), "default toString contains class name");
        check(!(dp2 instanceof java.io.Serializable), "not Serializable");
        check(new java.security.spec.DSAParameterSpec[0].getClass().getComponentType() == java.security.spec.DSAParameterSpec.class, "array component type");
        try {
            java.security.AlgorithmParameters ap = java.security.AlgorithmParameters.getInstance("DSA");
            ap.init(dp2);
            java.security.spec.DSAParameterSpec back = ap.getParameterSpec(java.security.spec.DSAParameterSpec.class);
            check(back != null, "AlgorithmParameters accepts DSAParameterSpec and returns one");
            check(p2.equals(back.getP()), "p survives engine init/getParameterSpec round-trip");
            check(q2.equals(back.getQ()), "q survives engine init/getParameterSpec round-trip");
            check(g2.equals(back.getG()), "g survives engine init/getParameterSpec round-trip");
            check(ap.getEncoded() != null && ap.getEncoded().length > 0, "ASN.1 encoding produced from spec");
        } catch (Exception e) {
            throw new AssertionError("AlgorithmParameters interop failed: " + e, e);
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
