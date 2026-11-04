/*
 * @test
 * @summary org.w3c.dom.ls.LSSerializer conformance: reflection shape checks.
 * @run main LSSerializerTest
 */

import java.lang.reflect.*;

public class LSSerializerTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ls.LSSerializer");
        check(c != null, "org.w3c.dom.ls.LSSerializer loads");
        check(c.getName().equals("org.w3c.dom.ls.LSSerializer"), "binary name is org.w3c.dom.ls.LSSerializer");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSSerializer is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSSerializer is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getDomConfig"), "declares getDomConfig");
        check(declares(c, "getNewLine"), "declares getNewLine");
        check(declares(c, "setNewLine"), "declares setNewLine");
        check(declares(c, "getFilter"), "declares getFilter");
        check(declares(c, "setFilter"), "declares setFilter");
        check(declares(c, "write"), "declares write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
