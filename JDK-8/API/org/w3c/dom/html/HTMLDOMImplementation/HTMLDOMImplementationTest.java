/*
 * @test
 * @summary org.w3c.dom.html.HTMLDOMImplementation conformance: reflection shape checks.
 * @run main HTMLDOMImplementationTest
 */

import java.lang.reflect.*;

public class HTMLDOMImplementationTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLDOMImplementation");
        check(c != null, "org.w3c.dom.html.HTMLDOMImplementation loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLDOMImplementation"), "binary name is org.w3c.dom.html.HTMLDOMImplementation");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLDOMImplementation is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLDOMImplementation is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.DOMImplementation")), "direct superinterfaces");
        check(declares(c, "createHTMLDocument"), "declares createHTMLDocument");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral + shape-lock ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.DOMImplementation.class.isAssignableFrom(org.w3c.dom.html.HTMLDOMImplementation.class), "HTMLDOMImplementation extends DOMImplementation");
        org.w3c.dom.html.HTMLDOMImplementation h = new org.w3c.dom.html.HTMLDOMImplementation() {
            public boolean hasFeature(String f, String v) { return false; }
            public Object getFeature(String f, String v) { return null; }
            public org.w3c.dom.DocumentType createDocumentType(String q, String p, String s) { return null; }
            public org.w3c.dom.Document createDocument(String ns, String q, org.w3c.dom.DocumentType dt) { return null; }
            public org.w3c.dom.html.HTMLDocument createHTMLDocument(String title) { return null; }
        };
        check(h.createHTMLDocument("t") == null, "createHTMLDocument routed");
        check(!h.hasFeature("HTML", "2.0") && h.getFeature("HTML", "2.0") == null, "anonymous implementation routes the base queries");
        check(h.createDocumentType("q", null, null) == null, "createDocumentType routed");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLDOMImplementation.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 1, "HTMLDOMImplementation declares exactly 1 methods");
        check(names.contains("createHTMLDocument"), "createHTMLDocument declared");
    }
}
