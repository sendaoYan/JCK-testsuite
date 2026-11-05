/*
 * @test
 * @summary org.w3c.dom.css.ViewCSS conformance: reflection shape checks.
 * @run main ViewCSSTest
 */

import java.lang.reflect.*;

public class ViewCSSTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.ViewCSS");
        check(c != null, "org.w3c.dom.css.ViewCSS loads");
        check(c.getName().equals("org.w3c.dom.css.ViewCSS"), "binary name is org.w3c.dom.css.ViewCSS");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.ViewCSS is public");
        check(c.isInterface(), "org.w3c.dom.css.ViewCSS is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.views.AbstractView")), "direct superinterfaces");
        check(declares(c, "getComputedStyle"), "declares getComputedStyle");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: ViewCSS contract (computed style channel) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(!org.w3c.dom.css.ViewCSS.class.isInstance(d), "Document is not a ViewCSS");
        org.w3c.dom.css.ViewCSS v = new org.w3c.dom.css.ViewCSS() {
            public org.w3c.dom.css.CSSStyleDeclaration getComputedStyle(org.w3c.dom.Element e, String pseudo) { return null; }
            public org.w3c.dom.views.DocumentView getDocument() { return null; }
        };
        check(v.getComputedStyle(null, null) == null, "computed style routed (null wiring)");
    }
}
