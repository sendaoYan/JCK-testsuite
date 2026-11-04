/*
 * @test
 * @summary java.security.PolicySpi conformance: white-box structure + black-box behavior.
 * @run main PolicySpiTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class PolicySpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PolicySpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // PolicySpi is the Service Provider Interface for Policy; engine methods are protected.
        // A minimal subclass exposes them for behavioral verification.
        class JckPolicySpi extends PolicySpi {
            CodeSource lastCs;
            ProtectionDomain lastPd;
            boolean refreshed;
            public boolean engineImplies(ProtectionDomain d, Permission p) { lastPd = d; return p.getName().startsWith("jck."); }
            public PermissionCollection engineGetPermissions(CodeSource cs) {
                lastCs = cs;
                Permissions perms = new Permissions();
                perms.add(new SecurityPermission("jck.spi"));
                return perms;
            }
            public void engineRefresh() { refreshed = true; }
            public PermissionCollection engineGetPermissions(ProtectionDomain d) { return super.engineGetPermissions(d); }
        }
        JckPolicySpi spi = new JckPolicySpi();
        ProtectionDomain pd = new ProtectionDomain(null, null);
        check(spi.engineImplies(pd, new SecurityPermission("jck.allow")), "engineImplies true");
        check(!spi.engineImplies(pd, new SecurityPermission("other")), "engineImplies false");
        check(spi.lastPd == pd, "domain observed");
        CodeSource cs = new CodeSource(null, (CodeSigner[]) null);
        check(spi.engineGetPermissions(cs).implies(new SecurityPermission("jck.spi")), "engineGetPermissions");
        check(spi.lastCs == cs, "codesource observed");
        spi.engineRefresh();
        check(spi.refreshed, "engineRefresh");
        // the base PolicySpi default engineGetPermissions(ProtectionDomain) returns an empty collection
        PermissionCollection viaPd = spi.engineGetPermissions(pd);
        check(viaPd != null, "default engineGetPermissions(ProtectionDomain)");
        check(java.lang.reflect.Modifier.isAbstract(PolicySpi.class.getModifiers()), "abstract");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
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
