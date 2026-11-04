/*
 * @test
 * @summary java.security.spec.RSAMultiPrimePrivateCrtKeySpec conformance: white-box structure + black-box behavior.
 * @run main RSAMultiPrimePrivateCrtKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAMultiPrimePrivateCrtKeySpec;
import java.security.spec.RSAOtherPrimeInfo;
import java.security.spec.NamedParameterSpec;

public class RSAMultiPrimePrivateCrtKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAMultiPrimePrivateCrtKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.spec.RSAPrivateKeySpec"), "superclass RSAPrivateKeySpec");
        checkMethods(c, new String[]{"getPublicExponent","getPrimeP","getPrimeQ","getPrimeExponentP","getPrimeExponentQ","getCrtCoefficient","getOtherPrimeInfo"});
    }

    static void testBlackBox() {
        BigInteger[] v = new BigInteger[8];
        for (int i = 0; i < 8; i++) v[i] = BigInteger.valueOf(i + 1);
        RSAOtherPrimeInfo[] other = new RSAOtherPrimeInfo[] {
            new RSAOtherPrimeInfo(new BigInteger("11"), new BigInteger("13"), new BigInteger("17"))
        };
        RSAMultiPrimePrivateCrtKeySpec spec = new RSAMultiPrimePrivateCrtKeySpec(
            v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], other);
        check(v[0].equals(spec.getModulus()), "getModulus");
        check(v[1].equals(spec.getPublicExponent()), "getPublicExponent");
        check(v[2].equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(v[3].equals(spec.getPrimeP()), "getPrimeP");
        check(v[4].equals(spec.getPrimeQ()), "getPrimeQ");
        check(v[5].equals(spec.getPrimeExponentP()), "getPrimeExponentP");
        check(v[6].equals(spec.getPrimeExponentQ()), "getPrimeExponentQ");
        check(v[7].equals(spec.getCrtCoefficient()), "getCrtCoefficient");
        check(spec.getParams() == null, "getParams null");

        RSAOtherPrimeInfo[] got = spec.getOtherPrimeInfo();
        check(got != other, "getOtherPrimeInfo defensive copy");
        check(got.length == 1, "getOtherPrimeInfo length");
        check(new BigInteger("11").equals(got[0].getPrime()), "other prime");
        got[0] = new RSAOtherPrimeInfo(new BigInteger("1"), new BigInteger("1"), new BigInteger("1"));
        check(spec.getOtherPrimeInfo()[0].getPrime().equals(new BigInteger("11")), "getOtherPrimeInfo unaffected by mutation");

        NamedParameterSpec params = NamedParameterSpec.X25519;
        RSAMultiPrimePrivateCrtKeySpec spec2 = new RSAMultiPrimePrivateCrtKeySpec(
            v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], other, params);
        check(params == spec2.getParams(), "10-arg getParams");
        check(spec2.getOtherPrimeInfo().length == 1, "10-arg getOtherPrimeInfo length");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
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
