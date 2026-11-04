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
        System.out.println("PASS (" + passed + " assertions)");
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
