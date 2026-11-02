/*
 * @test
 * @summary javax.tools.StandardJavaFileManager conformance: reflection shape checks.
 * @run main StandardJavaFileManagerTest
 */

import java.lang.reflect.*;

public class StandardJavaFileManagerTest {
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
        Class<?> c = Class.forName("javax.tools.StandardJavaFileManager");
        check(c != null, "javax.tools.StandardJavaFileManager loads");
        check(c.getName().equals("javax.tools.StandardJavaFileManager"), "binary name is javax.tools.StandardJavaFileManager");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.StandardJavaFileManager is public");
        check(c.isInterface(), "javax.tools.StandardJavaFileManager is an interface");
        check(ifaceNames(c).equals(set("javax.tools.JavaFileManager")), "direct superinterfaces");
        check(declares(c, "isSameFile"), "declares isSameFile");
        check(declares(c, "getJavaFileObjectsFromFiles"), "declares getJavaFileObjectsFromFiles");
        check(declares(c, "getJavaFileObjects"), "declares getJavaFileObjects");
        check(declares(c, "getJavaFileObjectsFromStrings"), "declares getJavaFileObjectsFromStrings");
        check(declares(c, "setLocation"), "declares setLocation");
        check(declares(c, "getLocation"), "declares getLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
