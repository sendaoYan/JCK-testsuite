/*
 * @test
 * @summary java.security.Policy conformance: white-box structure + black-box behavior.
 * @run main PolicyTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class PolicyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Policy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"getInstance", "getParameters", "getPermissions", "getPolicy", "getProvider", "getType", "implies", "refresh", "setPolicy"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // UNSUPPORTED_EMPTY_COLLECTION: read-only, implies nothing
        PermissionCollection empty = Policy.UNSUPPORTED_EMPTY_COLLECTION;
        check(!empty.implies(new AllPermission()), "empty implies");
        check(empty.isReadOnly(), "readOnly");
        try {
            empty.add(new SecurityPermission("x"));
            check(false, "add throws");
        } catch (SecurityException se) {
            check(true, "add throws");
        }
        check(!empty.elements().hasMoreElements(), "no elements");
        // getPolicy: an installed policy exists; it implies nothing and grants nothing
        Policy p = Policy.getPolicy();
        check(p != null, "getPolicy non-null");
        check(!p.implies(new ProtectionDomain(null, null), new AllPermission()), "implies false");
        PermissionCollection got = p.getPermissions(new CodeSource(null, (CodeSigner[]) null));
        check(!got.implies(new SecurityPermission("getPolicy")), "grants none");
        // setPolicy is permanently unsupported
        try {
            Policy.setPolicy(new Policy() {});
            check(false, "setPolicy throws");
        } catch (UnsupportedOperationException uoe) {
            check("Setting a system-wide Policy object is not supported".equals(uoe.getMessage()), "setPolicy message");
        }
        // refresh is a no-op but callable
        p.refresh();
        check(true, "refresh callable");
        // getInstance for any type: no Policy implementations installed
        try {
            Policy.getInstance("NoSuchPolicyType", null);
            check(false, "getInstance throws");
        } catch (NoSuchAlgorithmException nsae) {
            check(true, "no such type");
        }
        // subclass shape: getPermissions/implies defaults
        Policy custom = new Policy() {
            public PermissionCollection getPermissions(CodeSource cs) {
                Permissions perms = new Permissions();
                perms.add(new SecurityPermission("jck.custom"));
                return perms;
            }
        };
        check(custom.getPermissions((CodeSource) null).implies(new SecurityPermission("jck.custom")), "custom policy grants");
        check(!custom.getPermissions((CodeSource) null).implies(new SecurityPermission("other")), "custom policy limited");
        check(custom.getProvider() == null, "no provider");
        check(custom.getParameters() == null, "no parameters");

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
