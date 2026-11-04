/*
 * @test
 * @summary java.security.spec.MGF1ParameterSpec conformance: constructor and constants.
 * @run main MGF1ParameterSpecTest
 */

public class MGF1ParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.MGF1ParameterSpec s = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check("SHA-256".equals(s.getDigestAlgorithm()), "getDigestAlgorithm");
        check("SHA-256".equals(java.security.spec.MGF1ParameterSpec.SHA256.getDigestAlgorithm()), "SHA256 constant");
        // ---- 契约深化：常量全集/null 语义/值语义 ----
        check("SHA-1".equals(java.security.spec.MGF1ParameterSpec.SHA1.getDigestAlgorithm()), "SHA1 constant");
        check("SHA-224".equals(java.security.spec.MGF1ParameterSpec.SHA224.getDigestAlgorithm()), "SHA224 constant");
        check("SHA-256".equals(java.security.spec.MGF1ParameterSpec.SHA256.getDigestAlgorithm()), "SHA256 constant");
        check("SHA-384".equals(java.security.spec.MGF1ParameterSpec.SHA384.getDigestAlgorithm()), "SHA384 constant");
        check("SHA-512".equals(java.security.spec.MGF1ParameterSpec.SHA512.getDigestAlgorithm()), "SHA512 constant");
        check("SHA-512/224".equals(java.security.spec.MGF1ParameterSpec.SHA512_224.getDigestAlgorithm()), "SHA512_224 constant");
        check("SHA-512/256".equals(java.security.spec.MGF1ParameterSpec.SHA512_256.getDigestAlgorithm()), "SHA512_256 constant");
        checkThrowsMsg(java.lang.NullPointerException.class, () -> new java.security.spec.MGF1ParameterSpec(null),
                "digest algorithm", "null digest -> NPE");
        check("".equals(new java.security.spec.MGF1ParameterSpec("").getDigestAlgorithm()),
                "empty digest name accepted at spec level (no validation)");
        check("SHA3-256".equals(new java.security.spec.MGF1ParameterSpec("SHA3-256").getDigestAlgorithm()),
                "arbitrary digest name passthrough");
        check(new java.security.spec.MGF1ParameterSpec("SHA-256") instanceof java.security.spec.AlgorithmParameterSpec,
                "implements AlgorithmParameterSpec");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.MGF1ParameterSpec.class),
                "isAssignableFrom");
        check(!new java.security.spec.MGF1ParameterSpec("SHA-256").equals(new java.security.spec.MGF1ParameterSpec("SHA-256")),
                "no equals override: identity semantics");
        java.security.spec.MGF1ParameterSpec mgfOne = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check(mgfOne.hashCode() == mgfOne.hashCode(), "hashCode stable");
        check(mgfOne.toString().contains("MGF1ParameterSpec"), "toString contains class name");
        check(!(mgfOne instanceof java.io.Serializable), "not Serializable");
        java.security.spec.MGF1ParameterSpec[] allConstants = {
                java.security.spec.MGF1ParameterSpec.SHA1, java.security.spec.MGF1ParameterSpec.SHA224,
                java.security.spec.MGF1ParameterSpec.SHA256, java.security.spec.MGF1ParameterSpec.SHA384,
                java.security.spec.MGF1ParameterSpec.SHA512, java.security.spec.MGF1ParameterSpec.SHA512_224,
                java.security.spec.MGF1ParameterSpec.SHA512_256 };
        java.util.Set<String> distinct = new java.util.HashSet<String>();
        boolean allNonNull = true;
        for (java.security.spec.MGF1ParameterSpec c : allConstants) {
            if (c == null || c.getDigestAlgorithm() == null) allNonNull = false;
            else distinct.add(c.getDigestAlgorithm());
        }
        check(allNonNull, "all 7 standard constants non-null with non-null digest names");
        check(distinct.size() == 7, "all 7 constants name distinct digests");
        check(java.security.spec.MGF1ParameterSpec.class.getConstructors().length == 1,
                "single public constructor");
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
