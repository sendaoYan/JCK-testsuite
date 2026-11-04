/*
 * @test
 * @summary org.w3c.dom.NameList conformance: reflection shape checks.
 * @run main NameListTest
 */

import java.lang.reflect.*;

public class NameListTest {
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
        Class<?> c = Class.forName("org.w3c.dom.NameList");
        check(c != null, "org.w3c.dom.NameList loads");
        check(c.getName().equals("org.w3c.dom.NameList"), "binary name is org.w3c.dom.NameList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.NameList is public");
        check(c.isInterface(), "org.w3c.dom.NameList is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "getNamespaceURI"), "declares getNamespaceURI");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "contains"), "declares contains");
        check(declares(c, "containsNS"), "declares containsNS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
