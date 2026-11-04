/*
 * @test
 * @summary java.security.spec.PKCS8EncodedKeySpec conformance: constructor, getEncoded, getFormat.
 * @run main PKCS8EncodedKeySpecTest
 */

public class PKCS8EncodedKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] der = new byte[]{1,2,3};
        java.security.spec.PKCS8EncodedKeySpec s = new java.security.spec.PKCS8EncodedKeySpec(der);
        check(java.util.Arrays.equals(der, s.getEncoded()), "getEncoded");
        check("PKCS#8".equals(s.getFormat()), "getFormat");
        // ---- 契约深化：编码字节契约/防御拷贝/值语义/KeyFactory 闭环 ----
        checkThrows(NullPointerException.class, () -> new java.security.spec.PKCS8EncodedKeySpec(null),
                "ctor null bytes -> NPE");
        check(new java.security.spec.PKCS8EncodedKeySpec(new byte[0]).getEncoded().length == 0,
                "empty byte[] legal, getEncoded length 0");
        byte[] rawBytes = {1, 2, 3, 4};
        java.security.spec.PKCS8EncodedKeySpec s1 = new java.security.spec.PKCS8EncodedKeySpec(rawBytes);
        rawBytes[0] = 99;
        check(s1.getEncoded()[0] == 1, "constructor defensive copy");
        byte[] gotA = s1.getEncoded();
        byte[] gotB = s1.getEncoded();
        check(gotA != gotB && java.util.Arrays.equals(gotA, gotB), "getEncoded fresh copy per call, equal content");
        gotA[3] = 0;
        check(s1.getEncoded()[3] == 4, "mutating returned array isolated from spec");
        java.security.spec.PKCS8EncodedKeySpec s2 = new java.security.spec.PKCS8EncodedKeySpec(new byte[]{1, 2, 3, 4});
        check(java.util.Arrays.equals(s1.getEncoded(), s2.getEncoded()), "cross-instance content equality");
        byte[] sharedArr = {5};
        java.security.spec.PKCS8EncodedKeySpec specA = new java.security.spec.PKCS8EncodedKeySpec(sharedArr);
        java.security.spec.PKCS8EncodedKeySpec specB = new java.security.spec.PKCS8EncodedKeySpec(sharedArr);
        specA.getEncoded()[0] = 6;
        check(specB.getEncoded()[0] == 5, "two specs over the same array are fully independent");
        byte[] bigArr = new byte[1024];
        for (int i = 0; i < bigArr.length; i++) bigArr[i] = (byte) i;
        check(java.util.Arrays.equals(bigArr, new java.security.spec.PKCS8EncodedKeySpec(bigArr).getEncoded()),
                "1KB content fidelity");
        check(!s1.equals(s2), "no equals override: same content, different instances unequal");
        check(s1.hashCode() == s1.hashCode(), "hashCode stable");
        check(s1.toString().contains("PKCS8EncodedKeySpec"), "default toString contains class name");
        check(!(s1 instanceof java.io.Serializable), "not Serializable");
        check(s1 instanceof java.security.spec.EncodedKeySpec && s1 instanceof java.security.spec.KeySpec,
                "instanceof EncodedKeySpec/KeySpec");
        check("PKCS#8".equals(s1.getFormat()), "getFormat exact: PKCS#8");
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512);
            java.security.KeyPair kp = kpg.generateKeyPair();
            byte[] realEnc = kp.getPrivate().getEncoded();
            check(realEnc != null && "PKCS#8".equals(kp.getPrivate().getFormat()),
                    "real RSA private key uses PKCS#8 format name");
            java.security.spec.PKCS8EncodedKeySpec fromKey = new java.security.spec.PKCS8EncodedKeySpec(realEnc);
            check(java.util.Arrays.equals(realEnc, fromKey.getEncoded()), "wraps real PKCS#8 encoding unchanged");
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            java.security.PrivateKey restored = kf.generatePrivate(fromKey);
            check(restored.equals(kp.getPrivate()), "round-trip: spec -> KeyFactory.generatePrivate restores key");
        } catch (Exception e) {
            throw new AssertionError("KeyFactory interop failed: " + e, e);
        }        check(new java.security.spec.PKCS8EncodedKeySpec(new byte[0]).getFormat().equals("PKCS#8"),
                "empty spec still reports PKCS#8 format");
        check(!new java.security.spec.PKCS8EncodedKeySpec(new byte[0]).equals(new java.security.spec.PKCS8EncodedKeySpec(new byte[0])),
                "two empty specs are distinct objects (identity equality)");
        check(new java.security.spec.PKCS8EncodedKeySpec(new byte[]{1, 2, 3}).getEncoded().length == 3,
                "encoded length is exactly the input length");
        check(!"X.509".equals(s1.getFormat()), "format channel distinct from X.509 sibling");
        try {
            java.security.spec.PKCS8EncodedKeySpec interop = new java.security.spec.PKCS8EncodedKeySpec(new byte[]{1, 2, 3, 4});
            java.security.KeyPairGenerator kpg2 = java.security.KeyPairGenerator.getInstance("RSA");
            kpg2.initialize(512);
            java.security.KeyPair kp2 = kpg2.generateKeyPair();
            java.security.spec.PKCS8EncodedKeySpec spec2 = new java.security.spec.PKCS8EncodedKeySpec(kp2.getPrivate().getEncoded());
            java.security.KeyFactory.getInstance("RSA").generatePrivate(spec2);
            check(java.util.Arrays.equals(kp2.getPrivate().getEncoded(), spec2.getEncoded()),
                    "spec bytes unchanged after being consumed by KeyFactory");
        } catch (Exception e) {
            throw new AssertionError("interop failed: " + e, e);
        }
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
