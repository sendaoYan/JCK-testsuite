/*
 * @test
 * @summary org.w3c.dom.Document conformance: reflection shape checks.
 * @run main DocumentTest
 */

import java.lang.reflect.*;

public class DocumentTest {
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
        Class<?> c = Class.forName("org.w3c.dom.Document");
        check(c != null, "org.w3c.dom.Document loads");
        check(c.getName().equals("org.w3c.dom.Document"), "binary name is org.w3c.dom.Document");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.Document is public");
        check(c.isInterface(), "org.w3c.dom.Document is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getDoctype"), "declares getDoctype");
        check(declares(c, "getImplementation"), "declares getImplementation");
        check(declares(c, "getDocumentElement"), "declares getDocumentElement");
        check(declares(c, "createElement"), "declares createElement");
        check(declares(c, "createDocumentFragment"), "declares createDocumentFragment");
        check(declares(c, "createTextNode"), "declares createTextNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
