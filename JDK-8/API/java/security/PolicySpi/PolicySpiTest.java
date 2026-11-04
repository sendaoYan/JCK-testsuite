/*
 * @test
 * @summary java.security.PolicySpi conformance: abstract class hierarchy and key methods.
 * @run main PolicySpiTest
 */

public class PolicySpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    // Overrides ONLY the abstract engineImplies: every other engine method stays at its JDK default.
    static class RecSpi extends java.security.PolicySpi {
        java.security.ProtectionDomain lastPd; java.security.Permission lastPerm; int impliesCalls;
        protected boolean engineImplies(java.security.ProtectionDomain domain, java.security.Permission permission) {
            impliesCalls++; lastPd = domain; lastPerm = permission; return permission != null;
        }
        void doRefresh() { engineRefresh(); }
        java.security.PermissionCollection doGetCs() { return engineGetPermissions((java.security.CodeSource) null); }
        java.security.PermissionCollection doGetPd(java.security.ProtectionDomain pd) { return engineGetPermissions(pd); }
        boolean doImplies(java.security.ProtectionDomain pd, java.security.Permission p) { return engineImplies(pd, p); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.PolicySpi.class;

        // ---- structure: 1 abstract + 3 defaults ----
        check(c.getConstructor() != null, "public no-arg constructor");
        int abstractCount = 0;
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) { abstractCount++; names.add(m.getName()); }
        }
        check(abstractCount == 1 && names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("engineImplies"))),
                "exactly 1 abstract method: engineImplies");
        java.lang.reflect.Method m = c.getDeclaredMethod("engineImplies", java.security.ProtectionDomain.class, java.security.Permission.class);
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineImplies protected");
        m = c.getDeclaredMethod("engineRefresh");
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isProtected(m.getModifiers()),
                "engineRefresh protected default impl");
        m = c.getDeclaredMethod("engineGetPermissions", java.security.CodeSource.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineGetPermissions(CodeSource) default impl");
        m = c.getDeclaredMethod("engineGetPermissions", java.security.ProtectionDomain.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineGetPermissions(ProtectionDomain) default impl");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");

        // ---- engineImplies pass-through ----
        RecSpi spi = new RecSpi();
        java.security.ProtectionDomain pd =
                new java.security.ProtectionDomain(null, new java.security.Permissions());
        java.security.AllPermission ap = new java.security.AllPermission();
        check(spi.doImplies(pd, ap), "engineImplies delegates to subclass");
        check(spi.impliesCalls == 1 && spi.lastPd == pd && spi.lastPerm == ap, "engineImplies received exact arguments");
        check(!spi.doImplies(pd, null), "engineImplies null permission reaches subclass (subclass decides)");

        // ---- engineRefresh default is a silent no-op ----
        spi.doRefresh(); // must simply return

        // ---- engineGetPermissions defaults: shared empty unmodifiable collection ----
        java.security.PermissionCollection cs = spi.doGetCs();
        java.security.PermissionCollection pc = spi.doGetPd(pd);
        check(cs != null && pc != null, "default engineGetPermissions non-null");
        check(cs == pc, "both overloads share one empty collection instance");
        check(cs.getClass().getName().endsWith("UnsupportedEmptyCollection"),
                "default collection is Policy.UnsupportedEmptyCollection");
        check(!cs.implies(new java.security.AllPermission()), "default collection implies nothing");
        check(!cs.elements().hasMoreElements(), "default collection has no elements");
        check(cs.toString() != null, "default collection toString non-null");
        try { cs.add(new java.security.AllPermission()); check(false, "default collection add throws"); }
        catch (java.lang.SecurityException se) { /* javadoc: cannot add to this collection */ }
        catch (Throwable t) { throw new AssertionError("unexpected " + t); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.PolicySpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineGetPermissions"), "declares engineGetPermissions");
        check(ms.contains("engineImplies"), "declares engineImplies");
        check(ms.contains("engineRefresh"), "declares engineRefresh");

        // ---- contract deepening: defaults + UnsupportedEmptyCollection semantics ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
