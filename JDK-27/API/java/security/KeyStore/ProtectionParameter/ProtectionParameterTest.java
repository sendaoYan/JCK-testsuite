/*
 * @test
 * @summary java.security.KeyStore.ProtectionParameter conformance: white-box structure + black-box behavior.
 * @run main ProtectionParameterTest
 */

import java.lang.reflect.*;
import java.security.KeyStore;



public class ProtectionParameterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$ProtectionParameter");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // marker interface; both JDK implementations satisfy it
        KeyStore.ProtectionParameter pw = new KeyStore.PasswordProtection("x".toCharArray());
        KeyStore.ProtectionParameter cb = new KeyStore.CallbackHandlerProtection(callbacks -> { });
        check(pw instanceof KeyStore.ProtectionParameter, "PasswordProtection implements");
        check(cb instanceof KeyStore.ProtectionParameter, "CallbackHandlerProtection implements");
        // custom minimal implementation
        KeyStore.ProtectionParameter custom = new KeyStore.ProtectionParameter() { };
        check(custom != null, "anonymous implementation");
        // password-less load path accepts the interface end-to-end
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        check(ks.size() == 0, "default type empty load");
        check(KeyStore.getDefaultType() != null, "default type non-null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        Class<?> mc = Class.forName("java.security.KeyStore$ProtectionParameter");
        // marker interface: zero own methods (all inherited methods come from Object)
        int ownCount = 0;
        for (Method m : mc.getMethods()) {
            if (m.getDeclaringClass() == mc) ownCount++;
        }
        check(ownCount == 0, "marker interface declares no methods");

        // anonymous implementations and identity equality
        KeyStore.ProtectionParameter anon1 = new KeyStore.ProtectionParameter() { };
        KeyStore.ProtectionParameter anon2 = new KeyStore.ProtectionParameter() { };
        check(anon1.equals(anon1) && !anon1.equals(anon2),
                "anonymous instances identity equality");
        check(!anon1.equals(null), "anonymous marker not equal to null");

        // array covariance through the marker type (both JDK impls + an anonymous one)
        KeyStore.ProtectionParameter localPw = new KeyStore.PasswordProtection("x".toCharArray());
        KeyStore.ProtectionParameter localCb = new KeyStore.CallbackHandlerProtection(callbacks -> { });
        KeyStore.ProtectionParameter[] params = {localPw, localCb, anon1};
        check(params.length == 3 && mc.isInstance(params[2]), "parameter array covariance");

        // accepted as Builder protection input (marker value flows through Builder API)
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        KeyStore.Builder b = KeyStore.Builder.newInstance(ks, localPw);
        check(b.getKeyStore() == ks, "marker value accepted by Builder");
        check(b.getProtectionParameter("a") == localPw, "builder returns the same parameter");

        // DomainLoadStoreParameter's per-keystore map is typed by the marker
        java.util.Map<String, KeyStore.ProtectionParameter> map = new java.util.LinkedHashMap<>();
        map.put("keystore", localPw);
        java.security.DomainLoadStoreParameter d =
                new java.security.DomainLoadStoreParameter(java.net.URI.create("file:/x.cfg"), map);
        check(d.getProtectionParams().get("keystore") == localPw,
                "marker-typed map flows through DomainLoadStoreParameter");

        // PasswordProtection's Destroyable lifecycle is reachable through the marker type
        java.security.KeyStore.PasswordProtection destroyable =
                new java.security.KeyStore.PasswordProtection("z".toCharArray());
        check(mc.isInstance(destroyable), "destroyable instance is marker-typed");
        destroyable.destroy();
        check(destroyable.isDestroyed(), "destroy via marker-typed reference");

        // custom (non-Password/CallbackHandler) implementations are rejected by the
        // engine when recovering key entries — UnsupportedOperationException
        KeyStore jce = KeyStore.getInstance("JCEKS");
        jce.load(null, null);
        jce.setKeyEntry("s", new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"),
                "pw".toCharArray(), null);
        try {
            jce.getEntry("s", anon1);
            check(false, "custom ProtectionParameter rejected by getEntry");
        } catch (UnsupportedOperationException uoe) {
            check(true, "custom ProtectionParameter rejected by getEntry");
        }

        // Builder accepts an anonymous marker value verbatim
        KeyStore empty = KeyStore.getInstance("JKS");
        empty.load(null, null);
        KeyStore.Builder ab = KeyStore.Builder.newInstance(empty, anon2);
        check(ab.getKeyStore() == empty, "anonymous marker accepted by Builder");
        check(ab.getProtectionParameter("a") == anon2, "builder returns anonymous marker");

        // DomainLoadStoreParameter snapshot is unmodifiable, not just defensive
        boolean putRejected = false;
        try { d.getProtectionParams().put("x", null); }
        catch (UnsupportedOperationException uoe) { putRejected = true; }
        check(putRejected, "protection params view unmodifiable");

        // PasswordProtection destroy is idempotent through the concrete type
        destroyable.destroy();
        check(destroyable.isDestroyed(), "second destroy stays destroyed");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
