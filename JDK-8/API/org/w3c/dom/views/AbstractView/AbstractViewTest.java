/*
 * @test
 * @summary org.w3c.dom.views.AbstractView conformance: reflection shape checks.
 * @run main AbstractViewTest
 */

import java.lang.reflect.*;

public class AbstractViewTest {
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
        Class<?> c = Class.forName("org.w3c.dom.views.AbstractView");
        check(c != null, "org.w3c.dom.views.AbstractView loads");
        check(c.getName().equals("org.w3c.dom.views.AbstractView"), "binary name is org.w3c.dom.views.AbstractView");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.views.AbstractView is public");
        check(c.isInterface(), "org.w3c.dom.views.AbstractView is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getDocument"), "declares getDocument");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: AbstractView contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("Views", "2.0") && impl.getFeature("Views", "2.0") == null, "Views module unimplemented");
        final org.w3c.dom.views.DocumentView[] holder = new org.w3c.dom.views.DocumentView[1];
        org.w3c.dom.views.AbstractView v = new org.w3c.dom.views.AbstractView() {
            public org.w3c.dom.views.DocumentView getDocument() { return holder[0]; }
        };
        holder[0] = new org.w3c.dom.views.DocumentView() {
            public org.w3c.dom.views.AbstractView getDefaultView() { return v; }
        };
        check(v.getDocument() == holder[0], "getDocument returns the wired DocumentView");
        check(v.getDocument().getDefaultView() == v, "view/document pair is mutually linked");
    }
}
