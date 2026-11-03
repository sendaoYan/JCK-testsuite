/*
 * @test
 * @summary java.security.Permission conformance: white-box structure + black-box behavior.
 * @run main PermissionTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class PermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Permission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.security.Guard", "java.io.Serializable"});
        checkMethods(c, new String[]{"checkGuard", "equals", "getActions", "getName", "hashCode", "implies", "newPermissionCollection", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Concrete subclass drives the abstract Permission contract.
        class FilePerm extends Permission {
            FilePerm(String n) { super(n); }
            public boolean implies(Permission p) { return p instanceof FilePerm && getName().equals(p.getName()); }
            public boolean equals(Object o) { return o instanceof FilePerm && ((FilePerm) o).getName().equals(getName()); }
            public int hashCode() { return getName().hashCode(); }
            public String getActions() { return "read,write"; }
        }
        FilePerm fp = new FilePerm("a.txt");
        check(fp.getName().equals("a.txt"), "getName");
        check(fp.getActions().equals("read,write"), "getActions");
        check(fp.implies(new FilePerm("a.txt")), "implies self");
        check(!fp.implies(new FilePerm("b.txt")), "implies different name");
        check(!fp.implies(new SecurityPermission("x")), "implies other type");
        check(fp.equals(new FilePerm("a.txt")) && !fp.equals(new FilePerm("b.txt")), "equals");
        check(fp.hashCode() == new FilePerm("a.txt").hashCode(), "hashCode");
        check(fp.toString().contains("a.txt"), "toString contains name");
        // newPermissionCollection default returns null for plain subclasses
        check(fp.newPermissionCollection() == null,
            "Permission.newPermissionCollection default null");
        // Permission implements Guard: post-SecurityManager-removal checkGuard always throws
        try {
            fp.checkGuard(null);
            check(false, "checkGuard should throw");
        } catch (SecurityException e) {
            check("checking permissions is not supported".equals(e.getMessage()), "checkGuard message");
        }
        // SecurityPermission overrides newPermissionCollection to non-null
        check(new SecurityPermission("getPolicy").newPermissionCollection() != null,
            "SecurityPermission.newPermissionCollection non-null");

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
