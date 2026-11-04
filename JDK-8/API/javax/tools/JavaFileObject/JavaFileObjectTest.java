/*
 * @test
 * @summary javax.tools.JavaFileObject conformance: reflection shape checks.
 * @run main JavaFileObjectTest
 */

import java.lang.reflect.*;

public class JavaFileObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.tools.JavaFileObject");
        check(c != null, "javax.tools.JavaFileObject loads");
        check(c.getName().equals("javax.tools.JavaFileObject"), "binary name is javax.tools.JavaFileObject");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.JavaFileObject is public");
        check(c.isInterface(), "javax.tools.JavaFileObject is an interface");
        check(ifaceNames(c).equals(set("javax.tools.FileObject")), "direct superinterfaces");
        check(declares(c, "getKind"), "declares getKind");
        check(declares(c, "isNameCompatible"), "declares isNameCompatible");
        check(declares(c, "getNestingKind"), "declares getNestingKind");
        check(declares(c, "getAccessLevel"), "declares getAccessLevel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
