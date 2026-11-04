/*
 * @test
 * @summary javax.tools.DiagnosticListener conformance: reflection shape checks.
 * @run main DiagnosticListenerTest
 */

import java.lang.reflect.*;

public class DiagnosticListenerTest {
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
        Class<?> c = Class.forName("javax.tools.DiagnosticListener");
        check(c != null, "javax.tools.DiagnosticListener loads");
        check(c.getName().equals("javax.tools.DiagnosticListener"), "binary name is javax.tools.DiagnosticListener");
        check(Modifier.isPublic(c.getModifiers()), "javax.tools.DiagnosticListener is public");
        check(c.isInterface(), "javax.tools.DiagnosticListener is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "report"), "declares report");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
