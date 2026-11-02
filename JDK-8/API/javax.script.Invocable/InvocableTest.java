/*
 * @test
 * @summary javax.script.Invocable conformance: reflection shape checks.
 * @run main InvocableTest
 */

import java.lang.reflect.*;

public class InvocableTest {
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
        Class<?> c = Class.forName("javax.script.Invocable");
        check(c != null, "javax.script.Invocable loads");
        check(c.getName().equals("javax.script.Invocable"), "binary name is javax.script.Invocable");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.Invocable is public");
        check(c.isInterface(), "javax.script.Invocable is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "invokeMethod"), "declares invokeMethod");
        check(declares(c, "invokeFunction"), "declares invokeFunction");
        check(declares(c, "getInterface"), "declares getInterface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
