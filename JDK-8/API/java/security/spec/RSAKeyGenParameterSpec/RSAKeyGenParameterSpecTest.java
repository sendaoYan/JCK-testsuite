/*
 * @test
 * @summary java.security.spec.RSAKeyGenParameterSpec conformance: constructor and getters.
 * @run main RSAKeyGenParameterSpecTest
 */

public class RSAKeyGenParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.RSAKeyGenParameterSpec s = new java.security.spec.RSAKeyGenParameterSpec(2048, java.math.BigInteger.valueOf(65537));
        check(s.getKeysize() == 2048, "getKeysize");
        check(java.math.BigInteger.valueOf(65537).equals(s.getPublicExponent()), "getPublicExponent");
        check(s.getKeyParams() == null, "getKeyParams null");
        check(java.security.spec.RSAKeyGenParameterSpec.F4.intValue() == 65537, "F4 constant");
        // ---- 契约深化：常量精确值/构造边界/值语义/KeyPairGenerator 闭环 ----
        check(java.security.spec.RSAKeyGenParameterSpec.F0.intValue() == 3, "F0 == 3");
        check(java.security.spec.RSAKeyGenParameterSpec.F4.intValue() == 65537, "F4 == 65537");
        check(!java.security.spec.RSAKeyGenParameterSpec.F0.equals(java.security.spec.RSAKeyGenParameterSpec.F4), "F0 != F4");
        java.security.spec.RSAKeyGenParameterSpec rkg = new java.security.spec.RSAKeyGenParameterSpec(2048, java.security.spec.RSAKeyGenParameterSpec.F4);
        check(rkg.getKeysize() == 2048 && java.security.spec.RSAKeyGenParameterSpec.F4.equals(rkg.getPublicExponent()),
                "2-arg ctor round-trip");
        check(rkg.getKeyParams() == null, "2-arg ctor leaves keyParams null");
        java.security.spec.MGF1ParameterSpec mgfParam = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check(new java.security.spec.RSAKeyGenParameterSpec(2048, java.security.spec.RSAKeyGenParameterSpec.F4, mgfParam).getKeyParams() == mgfParam,
                "3-arg ctor keyParams passthrough");
        check(new java.security.spec.RSAKeyGenParameterSpec(-1, java.security.spec.RSAKeyGenParameterSpec.F4).getKeysize() == -1,
                "keysize not validated at spec level (actual behavior: negative accepted)");
        check(new java.security.spec.RSAKeyGenParameterSpec(2048, null).getPublicExponent() == null,
                "null public exponent accepted at spec level");
        check(rkg instanceof java.security.spec.AlgorithmParameterSpec, "implements AlgorithmParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.RSAKeyGenParameterSpec.class), "isAssignableFrom");
        check(!rkg.equals(new java.security.spec.RSAKeyGenParameterSpec(2048, java.security.spec.RSAKeyGenParameterSpec.F4)),
                "no equals override: identity semantics");
        check(rkg.hashCode() == rkg.hashCode(), "hashCode stable");
        check(rkg.toString().contains("RSAKeyGenParameterSpec"), "default toString contains class name");
        check(!(rkg instanceof java.io.Serializable), "not Serializable");
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(new java.security.spec.RSAKeyGenParameterSpec(512, java.security.spec.RSAKeyGenParameterSpec.F4));
            java.security.KeyPair kp = kpg.generateKeyPair();
            java.security.interfaces.RSAPublicKey rpub = (java.security.interfaces.RSAPublicKey) kp.getPublic();
            check(rpub.getModulus().bitLength() == 512, "spec drives keysize via KeyPairGenerator.initialize(spec)");
            check(rpub.getPublicExponent().intValue() == 65537, "spec drives public exponent F4");
        } catch (Exception e) {
            throw new AssertionError("KeyPairGenerator interop failed: " + e, e);
        }
        check(new java.security.spec.RSAKeyGenParameterSpec(0, java.security.spec.RSAKeyGenParameterSpec.F4).getKeysize() == 0,
                "keysize 0 accepted at spec level (no validation)");
        check(new java.security.spec.RSAKeyGenParameterSpec(512, java.security.spec.RSAKeyGenParameterSpec.F0).getPublicExponent().intValue() == 3,
                "F0 round-trip as exponent");
        check(new java.security.spec.RSAKeyGenParameterSpec(2048, java.security.spec.RSAKeyGenParameterSpec.F4, null).getKeyParams() == null,
                "3-arg ctor with null keyParams keeps it null");
        check(java.security.spec.RSAKeyGenParameterSpec.class.getConstructors().length == 2,
                "exactly two public constructors (2-arg and 3-arg)");
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
