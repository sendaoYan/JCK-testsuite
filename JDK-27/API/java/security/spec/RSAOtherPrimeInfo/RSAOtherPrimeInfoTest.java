/*
 * @test
 * @summary java.security.spec.RSAOtherPrimeInfo conformance: white-box structure + black-box behavior.
 * @run main RSAOtherPrimeInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

public class RSAOtherPrimeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAOtherPrimeInfo.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        checkMethods(c, new String[]{"getPrime","getExponent","getCrtCoefficient"});
    }

    static void testBlackBox() {
        BigInteger prime = new BigInteger("5");
        BigInteger exponent = new BigInteger("7");
        BigInteger crt = new BigInteger("9");
        RSAOtherPrimeInfo info = new RSAOtherPrimeInfo(prime, exponent, crt);
        check(prime.equals(info.getPrime()), "getPrime");
        check(exponent.equals(info.getExponent()), "getExponent");
        check(crt.equals(info.getCrtCoefficient()), "getCrtCoefficient");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContract();
        java.security.spec.RSAOtherPrimeInfo opi2 = new java.security.spec.RSAOtherPrimeInfo(
                java.math.BigInteger.valueOf(101), java.math.BigInteger.valueOf(103), java.math.BigInteger.valueOf(107));
        check(java.math.BigInteger.valueOf(101).equals(opi2.getPrime()), "distinct prime value round-trip");
        check(java.math.BigInteger.valueOf(103).equals(opi2.getExponent()), "distinct exponent value round-trip");
        check(java.math.BigInteger.valueOf(107).equals(opi2.getCrtCoefficient()), "distinct crtCoefficient value round-trip");
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：null 语义矩阵/引用保持/值语义 ----
        checkThrowsMsg(java.lang.NullPointerException.class,
                () -> new java.security.spec.RSAOtherPrimeInfo(null, java.math.BigInteger.ONE, java.math.BigInteger.valueOf(2)),
                "prime", "null prime -> NPE mentioning prime");
        checkThrowsMsg(java.lang.NullPointerException.class,
                () -> new java.security.spec.RSAOtherPrimeInfo(java.math.BigInteger.valueOf(3), null, java.math.BigInteger.valueOf(2)),
                "primeExponent", "null exponent -> NPE mentioning primeExponent");
        checkThrowsMsg(java.lang.NullPointerException.class,
                () -> new java.security.spec.RSAOtherPrimeInfo(java.math.BigInteger.valueOf(3), java.math.BigInteger.ONE, null),
                "crtCoefficient", "null crtCoefficient -> NPE mentioning crtCoefficient");
        checkThrows(java.lang.NullPointerException.class,
                () -> new java.security.spec.RSAOtherPrimeInfo(null, null, null), "all-null -> NPE");
        java.math.BigInteger op = java.math.BigInteger.valueOf(101);
        java.math.BigInteger oe = java.math.BigInteger.valueOf(103);
        java.math.BigInteger oc = java.math.BigInteger.valueOf(107);
        java.security.spec.RSAOtherPrimeInfo opi = new java.security.spec.RSAOtherPrimeInfo(op, oe, oc);
        check(opi.getPrime() == op, "getPrime returns the same BigInteger reference");
        check(opi.getExponent() == oe, "getExponent returns the same BigInteger reference");
        check(opi.getCrtCoefficient() == oc, "getCrtCoefficient returns the same BigInteger reference");
        check(java.security.spec.RSAOtherPrimeInfo.class.getInterfaces().length == 0, "implements no interfaces");
        check(java.security.spec.RSAOtherPrimeInfo.class.getConstructors().length == 1, "single public constructor");
        check(!opi.equals(new java.security.spec.RSAOtherPrimeInfo(op, oe, oc)), "no equals override: identity semantics");
        check(opi.equals(opi), "self-equality");
        check(opi.hashCode() == opi.hashCode(), "hashCode stable");
        check(opi.toString().contains("RSAOtherPrimeInfo"), "default toString contains class name");
        check(!(opi instanceof java.io.Serializable), "not Serializable");
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
