/*
 * @test
 * @summary javax.tools.Tool conformance: reflection shape checks.
 * @run main ToolTest
 */

import java.lang.reflect.*;

public class ToolTest {
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
        Class<?> c = Class.forName("javax.tools.Tool");
        check(c != null, "javax.tools.Tool loads");
        check(c.getName().equals("javax.tools.Tool"), "binary name is javax.tools.Tool");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.Tool is public");
        check(c.isInterface(), "javax.tools.Tool is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "run"), "declares run");
        check(declares(c, "getSourceVersions"), "declares getSourceVersions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
