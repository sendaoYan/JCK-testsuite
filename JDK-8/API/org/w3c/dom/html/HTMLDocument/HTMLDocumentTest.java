/*
 * @test
 * @summary org.w3c.dom.html.HTMLDocument conformance: reflection shape checks.
 * @run main HTMLDocumentTest
 */

import java.lang.reflect.*;

public class HTMLDocumentTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLDocument");
        check(c != null, "org.w3c.dom.html.HTMLDocument loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLDocument"), "binary name is org.w3c.dom.html.HTMLDocument");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLDocument is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLDocument is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Document")), "direct superinterfaces");
        check(declares(c, "getTitle"), "declares getTitle");
        check(declares(c, "setTitle"), "declares setTitle");
        check(declares(c, "getReferrer"), "declares getReferrer");
        check(declares(c, "getDomain"), "declares getDomain");
        check(declares(c, "getURL"), "declares getURL");
        check(declares(c, "getBody"), "declares getBody");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
