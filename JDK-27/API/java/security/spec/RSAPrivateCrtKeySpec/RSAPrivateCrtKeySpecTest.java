/*
 * @test
 * @summary java.security.spec.RSAPrivateCrtKeySpec conformance: white-box structure + black-box behavior.
 * @run main RSAPrivateCrtKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.NamedParameterSpec;

public class RSAPrivateCrtKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAPrivateCrtKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.spec.RSAPrivateKeySpec"), "superclass RSAPrivateKeySpec");
        checkMethods(c, new String[]{"getPublicExponent","getPrimeP","getPrimeQ","getPrimeExponentP","getPrimeExponentQ","getCrtCoefficient"});
    }

    static void testBlackBox() {
        BigInteger[] v = new BigInteger[8];
        for (int i = 0; i < 8; i++) v[i] = BigInteger.valueOf(i + 1);
        RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
        check(v[0].equals(spec.getModulus()), "getModulus");
        check(v[1].equals(spec.getPublicExponent()), "getPublicExponent");
        check(v[2].equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(v[3].equals(spec.getPrimeP()), "getPrimeP");
        check(v[4].equals(spec.getPrimeQ()), "getPrimeQ");
        check(v[5].equals(spec.getPrimeExponentP()), "getPrimeExponentP");
        check(v[6].equals(spec.getPrimeExponentQ()), "getPrimeExponentQ");
        check(v[7].equals(spec.getCrtCoefficient()), "getCrtCoefficient");
        check(spec.getParams() == null, "getParams null");

        NamedParameterSpec params = NamedParameterSpec.X25519;
        RSAPrivateCrtKeySpec spec2 = new RSAPrivateCrtKeySpec(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], params);
        check(params == spec2.getParams(), "9-arg getParams");
        check(v[3].equals(spec2.getPrimeP()), "9-arg getPrimeP");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：全 getter 往返/keyParams/值语义 ----
        java.math.BigInteger cm = java.math.BigInteger.valueOf(2);
        java.math.BigInteger ce = java.math.BigInteger.valueOf(3);
        java.math.BigInteger cd = java.math.BigInteger.valueOf(5);
        java.math.BigInteger cp = java.math.BigInteger.valueOf(7);
        java.math.BigInteger cq = java.math.BigInteger.valueOf(11);
        java.math.BigInteger cdp = java.math.BigInteger.valueOf(13);
        java.math.BigInteger cdq = java.math.BigInteger.valueOf(17);
        java.math.BigInteger cc = java.math.BigInteger.valueOf(19);
        java.security.spec.RSAPrivateCrtKeySpec crt = new java.security.spec.RSAPrivateCrtKeySpec(cm, ce, cd, cp, cq, cdp, cdq, cc);
        check(cm.equals(crt.getModulus()), "getModulus round-trip");
        check(cd.equals(crt.getPrivateExponent()), "getPrivateExponent round-trip");
        check(ce.equals(crt.getPublicExponent()), "getPublicExponent round-trip");
        check(cp.equals(crt.getPrimeP()), "getPrimeP round-trip");
        check(cq.equals(crt.getPrimeQ()), "getPrimeQ round-trip");
        check(cdp.equals(crt.getPrimeExponentP()), "getPrimeExponentP round-trip");
        check(cdq.equals(crt.getPrimeExponentQ()), "getPrimeExponentQ round-trip");
        check(cc.equals(crt.getCrtCoefficient()), "getCrtCoefficient round-trip");
        check(crt.getPrimeP() == cp && crt.getPrimeQ() == cq, "getters return the same BigInteger references");
        check(crt.getParams() == null, "8-arg ctor leaves keyParams null");
        java.security.spec.MGF1ParameterSpec crtParams = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check(new java.security.spec.RSAPrivateCrtKeySpec(cm, ce, cd, cp, cq, cdp, cdq, cc, crtParams).getParams() == crtParams,
                "9-arg ctor keyParams passthrough");
        check(new java.security.spec.RSAPrivateCrtKeySpec(null, null, null, null, null, null, null, null).getModulus() == null,
                "all-null ctor legal at spec level");
        check(crt instanceof java.security.spec.RSAPrivateKeySpec && crt instanceof java.security.spec.KeySpec,
                "extends RSAPrivateKeySpec, hence KeySpec");
        check(!crt.equals(new java.security.spec.RSAPrivateCrtKeySpec(cm, ce, cd, cp, cq, cdp, cdq, cc)),
                "no equals override: identity semantics");
        check(crt.hashCode() == crt.hashCode(), "hashCode stable");
        check(crt.toString().contains("RSAPrivateCrtKeySpec"), "default toString contains class name");
        check(!(crt instanceof java.io.Serializable), "not Serializable");
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
