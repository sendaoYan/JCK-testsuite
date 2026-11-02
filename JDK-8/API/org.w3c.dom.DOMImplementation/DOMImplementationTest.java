/*
 * @test
 * @summary org.w3c.dom.DOMImplementation conformance: reflection shape checks.
 * @run main DOMImplementationTest
 */

import java.lang.reflect.*;

public class DOMImplementationTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMImplementation");
        check(c != null, "org.w3c.dom.DOMImplementation loads");
        check(c.getName().equals("org.w3c.dom.DOMImplementation"), "binary name is org.w3c.dom.DOMImplementation");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMImplementation is public");
        check(c.isInterface(), "org.w3c.dom.DOMImplementation is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "hasFeature"), "declares hasFeature");
        check(declares(c, "createDocumentType"), "declares createDocumentType");
        check(declares(c, "createDocument"), "declares createDocument");
        check(declares(c, "getFeature"), "declares getFeature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
