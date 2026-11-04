/*
 * @test
 * @summary javax.tools.JavaFileManager.Location conformance: reflection shape checks.
 * @run main LocationTest
 */

import java.lang.reflect.*;

public class LocationTest {
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
        Class<?> c = Class.forName("javax.tools.JavaFileManager$Location");
        check(c != null, "javax.tools.JavaFileManager$Location loads");
        check(c.getName().equals("javax.tools.JavaFileManager$Location"), "binary name is javax.tools.JavaFileManager$Location");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.JavaFileManager$Location is public");
        check(c.isInterface(), "javax.tools.JavaFileManager$Location is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "isOutputLocation"), "declares isOutputLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
