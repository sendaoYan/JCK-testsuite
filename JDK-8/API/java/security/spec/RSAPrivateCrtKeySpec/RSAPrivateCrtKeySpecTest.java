/*
 * @test
 * @summary java.security.spec.RSAPrivateCrtKeySpec conformance: constructor and getters.
 * @run main RSAPrivateCrtKeySpecTest
 */

public class RSAPrivateCrtKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger one = java.math.BigInteger.ONE;
        java.math.BigInteger two = java.math.BigInteger.valueOf(2);
        java.security.spec.RSAPrivateCrtKeySpec s = new java.security.spec.RSAPrivateCrtKeySpec(
            two, one, one, two, two, one, one, one);
        check(two.equals(s.getModulus()), "getModulus");
        check(one.equals(s.getPrivateExponent()), "getPrivateExponent");
        check(two.equals(s.getPrimeP()), "getPrimeP");
        check(two.equals(s.getPrimeQ()), "getPrimeQ");
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
        java.security.spec.RSAPrivateCrtKeySpec crtNulls = new java.security.spec.RSAPrivateCrtKeySpec(null, null, null, null, null, null, null, null);
        check(crtNulls.getPublicExponent() == null, "null ctor: publicExponent null");
        check(crtNulls.getPrimeP() == null && crtNulls.getPrimeQ() == null, "null ctor: primes null");
        check(crtNulls.getPrimeExponentP() == null && crtNulls.getPrimeExponentQ() == null, "null ctor: prime exponents null");
        check(crtNulls.getCrtCoefficient() == null, "null ctor: crtCoefficient null");
        check(crtNulls.getPrivateExponent() == null, "null ctor: privateExponent null");
        System.out.println("PASS (" + passed + " assertions)");
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
