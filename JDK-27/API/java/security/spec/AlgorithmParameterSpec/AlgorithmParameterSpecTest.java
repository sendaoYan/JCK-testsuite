/*
 * @test
 * @summary java.security.spec.AlgorithmParameterSpec conformance: white-box structure + black-box behavior.
 * @run main AlgorithmParameterSpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;

public class AlgorithmParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.spec.AlgorithmParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaredMethods().length == 0, "no methods (marker interface)");
    }

    static void testBlackBox() {
        DSAParameterSpec d = new DSAParameterSpec(BigInteger.ONE, BigInteger.TWO, BigInteger.TEN);
        check(d instanceof AlgorithmParameterSpec, "DSAParameterSpec implements AlgorithmParameterSpec");
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
        // ---- 契约深化：标记接口完整语义 ----
        Class<?> aps2 = java.security.spec.AlgorithmParameterSpec.class;
        check(aps2.getInterfaces().length == 0, "marker interface has no super-interfaces");
        check(aps2.getDeclaredMethods().length == 0, "marker interface declares no methods");
        check(java.lang.reflect.Modifier.isPublic(aps2.getModifiers()), "public interface");
        check(!aps2.isAnnotation() && !aps2.isEnum(), "plain interface: not annotation/enum");
        check(aps2.getTypeParameters().length == 0, "not generic");
        java.math.BigInteger bp = java.math.BigInteger.valueOf(101);
        java.math.BigInteger bq = java.math.BigInteger.valueOf(103);
        java.math.BigInteger bg = java.math.BigInteger.valueOf(107);
        check(new java.security.spec.DSAParameterSpec(bp, bq, bg) instanceof java.security.spec.AlgorithmParameterSpec, "DSAParameterSpec instanceof AlgorithmParameterSpec");
        check(new java.security.spec.DSAGenParameterSpec(2048, 256) instanceof java.security.spec.AlgorithmParameterSpec, "DSAGenParameterSpec instanceof AlgorithmParameterSpec");
        check(new java.security.spec.MGF1ParameterSpec("SHA-256") instanceof java.security.spec.AlgorithmParameterSpec, "MGF1ParameterSpec instanceof AlgorithmParameterSpec");
        check(new java.security.spec.PSSParameterSpec(32) instanceof java.security.spec.AlgorithmParameterSpec, "PSSParameterSpec instanceof AlgorithmParameterSpec");
        check(new java.security.spec.RSAKeyGenParameterSpec(2048, java.security.spec.RSAKeyGenParameterSpec.F4) instanceof java.security.spec.AlgorithmParameterSpec, "RSAKeyGenParameterSpec instanceof AlgorithmParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.DSAParameterSpec.class), "isAssignableFrom DSAParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.DSAGenParameterSpec.class), "isAssignableFrom DSAGenParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.MGF1ParameterSpec.class), "isAssignableFrom MGF1ParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.PSSParameterSpec.class), "isAssignableFrom PSSParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.RSAKeyGenParameterSpec.class), "isAssignableFrom RSAKeyGenParameterSpec");
        check(!java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.DSAPrivateKeySpec.class), "KeySpec hierarchy is not under AlgorithmParameterSpec");
        boolean apsObjectOnly = true;
        for (java.lang.reflect.Method m2 : aps2.getMethods()) {
            if (!m2.getDeclaringClass().equals(Object.class)) apsObjectOnly = false;
        }
        check(apsObjectOnly, "full public method surface is Object-only");
        java.security.spec.AlgorithmParameterSpec nilSpec = null;
        check(nilSpec == null, "null is a legal AlgorithmParameterSpec-typed value");
        check(new java.security.spec.AlgorithmParameterSpec[0].getClass().getComponentType() == aps2, "array component type identity");
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
