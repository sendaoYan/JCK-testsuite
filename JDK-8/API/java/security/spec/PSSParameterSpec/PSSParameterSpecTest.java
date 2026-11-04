/*
 * @test
 * @summary java.security.spec.PSSParameterSpec conformance: constructors and getters.
 * @run main PSSParameterSpecTest
 */

public class PSSParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.PSSParameterSpec s = new java.security.spec.PSSParameterSpec(32);
        check(s.getSaltLength() == 32, "getSaltLength");
        java.security.spec.PSSParameterSpec s2 = new java.security.spec.PSSParameterSpec(
            "SHA-256", "MGF1", new java.security.spec.MGF1ParameterSpec("SHA-256"), 32, 1);
        check("SHA-256".equals(s2.getDigestAlgorithm()), "getDigestAlgorithm");
        check("MGF1".equals(s2.getMGFAlgorithm()), "getMGFAlgorithm");
        check(s2.getSaltLength() == 32, "getSaltLength 2");
        check(s2.getTrailerField() == 1, "getTrailerField");
        check(java.security.spec.PSSParameterSpec.DEFAULT.getSaltLength() == 20, "DEFAULT");
        // ---- 契约深化：常量精确值/构造矩阵/值语义 ----
        check(java.security.spec.PSSParameterSpec.TRAILER_FIELD_BC == 1, "TRAILER_FIELD_BC == 1");
        java.security.spec.PSSParameterSpec def = java.security.spec.PSSParameterSpec.DEFAULT;
        check("SHA-1".equals(def.getDigestAlgorithm()), "DEFAULT digest SHA-1");
        check("MGF1".equals(def.getMGFAlgorithm()), "DEFAULT mgf MGF1");
        check(def.getMGFParameters() == java.security.spec.MGF1ParameterSpec.SHA1, "DEFAULT mgfParameters is MGF1ParameterSpec.SHA1");
        check(def.getSaltLength() == 20, "DEFAULT salt 20");
        check(def.getTrailerField() == 1, "DEFAULT trailer 1");
        java.security.spec.PSSParameterSpec byInt = new java.security.spec.PSSParameterSpec(32);
        check("SHA-1".equals(byInt.getDigestAlgorithm()) && "MGF1".equals(byInt.getMGFAlgorithm()),
                "int ctor defaults digest SHA-1 / mgf MGF1");
        check(byInt.getMGFParameters() == java.security.spec.MGF1ParameterSpec.SHA1, "int ctor default mgfParameters is SHA1 singleton");
        check(byInt.getTrailerField() == 1, "int ctor default trailer 1");
        check(new java.security.spec.PSSParameterSpec(0).getSaltLength() == 0, "saltLen 0 boundary valid");
        checkThrowsMsg(java.lang.IllegalArgumentException.class, () -> new java.security.spec.PSSParameterSpec(-1),
                "saltLen", "int ctor saltLen -1 -> IAE");
        checkThrowsMsg(java.lang.NullPointerException.class,
                () -> new java.security.spec.PSSParameterSpec(null, "MGF1", java.security.spec.MGF1ParameterSpec.SHA1, 20, 1),
                "digest", "null digestAlgorithm -> NPE");
        checkThrowsMsg(java.lang.NullPointerException.class,
                () -> new java.security.spec.PSSParameterSpec("SHA-1", null, java.security.spec.MGF1ParameterSpec.SHA1, 20, 1),
                "mask generation", "null mgfAlgorithm -> NPE");
        checkThrowsMsg(java.lang.IllegalArgumentException.class,
                () -> new java.security.spec.PSSParameterSpec("SHA-1", "MGF1", java.security.spec.MGF1ParameterSpec.SHA1, -1, 1),
                "saltLen", "saltLen -1 -> IAE");
        checkThrowsMsg(java.lang.IllegalArgumentException.class,
                () -> new java.security.spec.PSSParameterSpec("SHA-1", "MGF1", java.security.spec.MGF1ParameterSpec.SHA1, 20, -1),
                "trailerField", "trailerField -1 -> IAE");
        java.security.spec.PSSParameterSpec trailer2 = new java.security.spec.PSSParameterSpec(
                "SHA-1", "MGF1", java.security.spec.MGF1ParameterSpec.SHA1, 20, 2);
        check(trailer2.getTrailerField() == 2, "trailerField 2 accepted (only negatives rejected)");
        java.security.spec.PSSParameterSpec nullMgf = new java.security.spec.PSSParameterSpec(
                "SHA-1", "MGF1", null, 20, 1);
        check(nullMgf.getMGFParameters() == null, "null mgfSpec accepted, getter null");
        java.security.spec.MGF1ParameterSpec mgf512 = new java.security.spec.MGF1ParameterSpec("SHA-512");
        java.security.spec.PSSParameterSpec full = new java.security.spec.PSSParameterSpec(
                "SHA-512", "MGF1", mgf512, 64, 1);
        check("SHA-512".equals(full.getDigestAlgorithm()) && "MGF1".equals(full.getMGFAlgorithm()),
                "full ctor digest/mgf round-trip");
        check(full.getMGFParameters() == mgf512 && full.getSaltLength() == 64 && full.getTrailerField() == 1,
                "full ctor mgfParams/salt/trailer round-trip");
        check(full instanceof java.security.spec.AlgorithmParameterSpec, "implements AlgorithmParameterSpec");
        check(!full.equals(new java.security.spec.PSSParameterSpec("SHA-512", "MGF1", mgf512, 64, 1)),
                "no equals override: identity semantics");
        check(full.hashCode() == full.hashCode(), "hashCode stable");
        check(!(full instanceof java.io.Serializable), "not Serializable");
        check(def.toString().contains("SHA-1") && def.toString().contains("SaltLength: 20"),
                "JDK-8 toString format: MD/MGF/SaltLength/TrailerField lines");
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
