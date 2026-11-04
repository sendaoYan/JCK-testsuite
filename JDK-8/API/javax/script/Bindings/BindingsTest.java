/*
 * @test
 * @summary javax.script.Bindings conformance: reflection shape checks.
 * @run main BindingsTest
 */

import java.lang.reflect.*;

public class BindingsTest {
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
        Class<?> c = Class.forName("javax.script.Bindings");
        check(c != null, "javax.script.Bindings loads");
        check(c.getName().equals("javax.script.Bindings"), "binary name is javax.script.Bindings");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.Bindings is public");
        check(c.isInterface(), "javax.script.Bindings is an interface");
        check(ifaceNames(c).equals(set("java.util.Map")), "direct superinterfaces");
        check(declares(c, "put"), "declares put");
        check(declares(c, "putAll"), "declares putAll");
        check(declares(c, "containsKey"), "declares containsKey");
        check(declares(c, "get"), "declares get");
        check(declares(c, "remove"), "declares remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
