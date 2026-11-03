/*
 * @test
 * @summary java.security.SecureClassLoader conformance: white-box structure + black-box behavior.
 * @run main SecureClassLoaderTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class SecureClassLoaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.SecureClassLoader");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.ClassLoader"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Subclass the protected constructors; delegation to parent loader works
        SecureClassLoader scl = new SecureClassLoader("jck-secure", SecureClassLoader.class.getClassLoader()) {};
        check("jck-secure".equals(scl.getName()), "named loader");
        check(scl.toString().contains("SecureClassLoader"), "toString class name");
        check(scl.loadClass("java.lang.String") == String.class, "delegation");
        check(scl.getParent() == SecureClassLoader.class.getClassLoader(), "parent");
        // unnamed constructor
        SecureClassLoader anon = new SecureClassLoader(null) {};
        check(anon.loadClass("java.util.ArrayList") == java.util.ArrayList.class, "delegation unnamed");
        // getPermissions: JDK-27 default returns an empty collection (no location-based grants);
        // a subclass widens access by overriding it publicly
        class Exposing extends SecureClassLoader {
            Exposing() { super(null); }
            public PermissionCollection getPermissions(CodeSource cs) { return super.getPermissions(cs); }
        }
        class Permissive extends SecureClassLoader {
            CodeSource seen;
            Permissive() { super(null); }
            public PermissionCollection getPermissions(CodeSource cs) {
                seen = cs;
                Permissions p = new Permissions();
                p.add(new AllPermission());
                return p;
            }
        }
        CodeSource cs = new CodeSource(new java.net.URL("file:/tmp/jck.jar"), (CodeSigner[]) null);
        PermissionCollection dpc = new Exposing().getPermissions(cs);
        check(dpc != null, "default getPermissions non-null");
        check(!dpc.implies(new SecurityPermission("x")), "default empty");
        check(!dpc.implies(new java.io.FilePermission("/tmp/jck.jar", "read")), "no location grant post-SMR");
        Permissive per = new Permissive();
        PermissionCollection wide = per.getPermissions(cs);
        check(per.seen == cs, "override observes CodeSource");
        check(wide.implies(new SecurityPermission("anything")), "overridden grants all");

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
