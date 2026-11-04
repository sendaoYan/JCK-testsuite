/*
 * @test
 * @summary java.security.spec.KeySpec conformance: interface hierarchy and key methods.
 * @run main KeySpecTest
 */

public class KeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.spec.KeySpec");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());

        // ---- 契约深化：标记接口完整语义 ----
        Class<?> ks2 = java.security.spec.KeySpec.class;
        check(ks2.getInterfaces().length == 0, "marker interface has no super-interfaces");
        check(ks2.getDeclaredMethods().length == 0, "marker interface declares no methods");
        check(java.lang.reflect.Modifier.isPublic(ks2.getModifiers()), "public interface");
        check(!ks2.isAnnotation() && !ks2.isEnum(), "plain interface: not annotation/enum");
        byte[] kb = {1};
        java.math.BigInteger k1 = java.math.BigInteger.ONE;
        check(new java.security.spec.PKCS8EncodedKeySpec(kb) instanceof java.security.spec.KeySpec, "PKCS8EncodedKeySpec instanceof KeySpec");
        check(new java.security.spec.X509EncodedKeySpec(kb) instanceof java.security.spec.KeySpec, "X509EncodedKeySpec instanceof KeySpec");
        check(new java.security.spec.DSAPrivateKeySpec(k1, k1, k1, k1) instanceof java.security.spec.KeySpec, "DSAPrivateKeySpec instanceof KeySpec");
        check(new java.security.spec.DSAPublicKeySpec(k1, k1, k1, k1) instanceof java.security.spec.KeySpec, "DSAPublicKeySpec instanceof KeySpec");
        check(new java.security.spec.RSAPrivateKeySpec(k1, k1) instanceof java.security.spec.KeySpec, "RSAPrivateKeySpec instanceof KeySpec");
        check(new java.security.spec.RSAPublicKeySpec(k1, k1) instanceof java.security.spec.KeySpec, "RSAPublicKeySpec instanceof KeySpec");
        check(new java.security.spec.RSAPrivateCrtKeySpec(k1, k1, k1, k1, k1, k1, k1, k1) instanceof java.security.spec.KeySpec, "RSAPrivateCrtKeySpec instanceof KeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.EncodedKeySpec.class), "isAssignableFrom EncodedKeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.PKCS8EncodedKeySpec.class), "isAssignableFrom PKCS8EncodedKeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.X509EncodedKeySpec.class), "isAssignableFrom X509EncodedKeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.DSAPrivateKeySpec.class), "isAssignableFrom DSAPrivateKeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.DSAPublicKeySpec.class), "isAssignableFrom DSAPublicKeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.RSAPrivateKeySpec.class), "isAssignableFrom RSAPrivateKeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.RSAPublicKeySpec.class), "isAssignableFrom RSAPublicKeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.RSAPrivateCrtKeySpec.class), "isAssignableFrom RSAPrivateCrtKeySpec");
        check(!java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.DSAParameterSpec.class), "AlgorithmParameterSpec family is not KeySpec");
        java.security.spec.KeySpec nilKs = null;
        check(nilKs == null, "null is a legal KeySpec-typed value");
        check(new java.security.spec.KeySpec[0].getClass().getComponentType() == ks2, "array component type identity");
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
