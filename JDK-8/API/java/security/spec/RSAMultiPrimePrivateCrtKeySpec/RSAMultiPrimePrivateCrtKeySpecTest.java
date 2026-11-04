/*
 * @test
 * @summary java.security.spec.RSAMultiPrimePrivateCrtKeySpec conformance: constructor and getters.
 * @run main RSAMultiPrimePrivateCrtKeySpecTest
 */

public class RSAMultiPrimePrivateCrtKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger one = java.math.BigInteger.ONE;
        java.math.BigInteger two = java.math.BigInteger.valueOf(2);
        java.security.spec.RSAOtherPrimeInfo[] others = new java.security.spec.RSAOtherPrimeInfo[]{
            new java.security.spec.RSAOtherPrimeInfo(java.math.BigInteger.valueOf(3), java.math.BigInteger.ONE, java.math.BigInteger.valueOf(2))
        };
        java.security.spec.RSAMultiPrimePrivateCrtKeySpec s = new java.security.spec.RSAMultiPrimePrivateCrtKeySpec(
            two, one, one, two, two, one, one, one, others);
        check(two.equals(s.getModulus()), "getModulus");
        check(one.equals(s.getPublicExponent()), "getPublicExponent");
        check(one.equals(s.getPrivateExponent()), "getPrivateExponent");
        check(s.getOtherPrimeInfo().length == 1, "getOtherPrimeInfo");
        // ---- 契约深化：otherPrimeInfo 契约/防御拷贝/值语义 ----
        java.math.BigInteger m = java.math.BigInteger.valueOf(2);
        java.math.BigInteger e = java.math.BigInteger.valueOf(3);
        java.math.BigInteger d = java.math.BigInteger.valueOf(5);
        java.math.BigInteger pp = java.math.BigInteger.valueOf(7);
        java.math.BigInteger qq = java.math.BigInteger.valueOf(11);
        java.math.BigInteger dp = java.math.BigInteger.valueOf(13);
        java.math.BigInteger dq = java.math.BigInteger.valueOf(17);
        java.math.BigInteger c = java.math.BigInteger.valueOf(19);
        java.security.spec.RSAOtherPrimeInfo o1 = new java.security.spec.RSAOtherPrimeInfo(
                java.math.BigInteger.valueOf(101), java.math.BigInteger.valueOf(103), java.math.BigInteger.valueOf(107));
        java.security.spec.RSAOtherPrimeInfo o2 = new java.security.spec.RSAOtherPrimeInfo(
                java.math.BigInteger.valueOf(109), java.math.BigInteger.valueOf(113), java.math.BigInteger.valueOf(127));
        java.security.spec.RSAOtherPrimeInfo[] othersArr = {o1, o2};
        java.security.spec.RSAMultiPrimePrivateCrtKeySpec mp = new java.security.spec.RSAMultiPrimePrivateCrtKeySpec(
                m, e, d, pp, qq, dp, dq, c, othersArr);
        check(m.equals(mp.getModulus()), "getModulus round-trip");
        check(e.equals(mp.getPublicExponent()), "getPublicExponent round-trip");
        check(d.equals(mp.getPrivateExponent()), "getPrivateExponent round-trip");
        check(pp.equals(mp.getPrimeP()), "getPrimeP round-trip");
        check(qq.equals(mp.getPrimeQ()), "getPrimeQ round-trip");
        check(dp.equals(mp.getPrimeExponentP()), "getPrimeExponentP round-trip");
        check(dq.equals(mp.getPrimeExponentQ()), "getPrimeExponentQ round-trip");
        check(c.equals(mp.getCrtCoefficient()), "getCrtCoefficient round-trip");
        check(mp.getOtherPrimeInfo().length == 2, "otherPrimeInfo length preserved");
        check(mp.getOtherPrimeInfo()[0] == o1 && mp.getOtherPrimeInfo()[1] == o2, "otherPrimeInfo elements preserved by identity");
        othersArr[0] = null;
        check(mp.getOtherPrimeInfo()[0] == o1, "constructor clones the otherPrimeInfo array (defensive copy)");
        java.security.spec.RSAOtherPrimeInfo[] gotArr1 = mp.getOtherPrimeInfo();
        java.security.spec.RSAOtherPrimeInfo[] gotArr2 = mp.getOtherPrimeInfo();
        check(gotArr1 != gotArr2 && gotArr1.length == gotArr2.length, "getOtherPrimeInfo returns a fresh array per call");
        gotArr1[0] = null;
        check(mp.getOtherPrimeInfo()[0] == o1, "mutating returned array isolated from spec");
        java.security.spec.RSAMultiPrimePrivateCrtKeySpec mpNull = new java.security.spec.RSAMultiPrimePrivateCrtKeySpec(
                m, e, d, pp, qq, dp, dq, c, null);
        check(mpNull.getOtherPrimeInfo() == null, "null otherPrimeInfo accepted, getter returns null");
        checkThrowsMsg(java.lang.IllegalArgumentException.class,
                () -> new java.security.spec.RSAMultiPrimePrivateCrtKeySpec(m, e, d, pp, qq, dp, dq, c, new java.security.spec.RSAOtherPrimeInfo[0]),
                "empty", "empty otherPrimeInfo array -> IAE");
        java.security.spec.MGF1ParameterSpec mpParams = new java.security.spec.MGF1ParameterSpec("SHA-256");
        java.security.spec.RSAMultiPrimePrivateCrtKeySpec mp10 = new java.security.spec.RSAMultiPrimePrivateCrtKeySpec(
                m, e, d, pp, qq, dp, dq, c, new java.security.spec.RSAOtherPrimeInfo[]{o1}, mpParams);
        check(mp10.getParams() == mpParams, "10-arg ctor keyParams passthrough via RSAPrivateKeySpec.getParams");
        check(mp instanceof java.security.spec.RSAPrivateKeySpec && mp instanceof java.security.spec.KeySpec,
                "extends RSAPrivateKeySpec, hence KeySpec");
        check(!mp.equals(new java.security.spec.RSAMultiPrimePrivateCrtKeySpec(m, e, d, pp, qq, dp, dq, c,
                new java.security.spec.RSAOtherPrimeInfo[]{o1, o2})), "no equals override: identity semantics");
        check(mp.hashCode() == mp.hashCode(), "hashCode stable");
        check(!(mp instanceof java.io.Serializable), "not Serializable");
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
