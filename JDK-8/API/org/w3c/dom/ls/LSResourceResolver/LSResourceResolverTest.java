/*
 * @test
 * @summary org.w3c.dom.ls.LSResourceResolver conformance: reflection shape checks.
 * @run main LSResourceResolverTest
 */

import java.lang.reflect.*;

public class LSResourceResolverTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ls.LSResourceResolver");
        check(c != null, "org.w3c.dom.ls.LSResourceResolver loads");
        check(c.getName().equals("org.w3c.dom.ls.LSResourceResolver"), "binary name is org.w3c.dom.ls.LSResourceResolver");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSResourceResolver is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSResourceResolver is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "resolveResource"), "declares resolveResource");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSResourceResolver contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.ls.LSResourceResolver r = new org.w3c.dom.ls.LSResourceResolver() {
            public org.w3c.dom.ls.LSInput resolveResource(String type, String ns, String publicId, String systemId, String baseURI) { return null; }
        };
        check(r.resolveResource("http://www.w3.org/2001/XMLSchema", null, null, "x.xsd", null) == null, "returning null means no resolution");
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.DOMConfiguration cfg = p.getDomConfig();
        check(cfg.canSetParameter("resource-resolver", r), "DomConfig accepts resource-resolver");
        cfg.setParameter("resource-resolver", r);
        check(cfg.getParameter("resource-resolver") == r, "resource-resolver round-trips through DomConfig");
        org.w3c.dom.ls.LSInput supplied = ls.createLSInput();
        org.w3c.dom.ls.LSResourceResolver r2 = new org.w3c.dom.ls.LSResourceResolver() {
            public org.w3c.dom.ls.LSInput resolveResource(String type, String ns, String publicId, String systemId, String baseURI) { return supplied; }
        };
        check(r2.resolveResource(null, null, null, null, null) == supplied, "resolver can hand back a prepared LSInput");
        // resolver signature shape
        java.lang.reflect.Method m = org.w3c.dom.ls.LSResourceResolver.class.getMethod("resolveResource",
            String.class, String.class, String.class, String.class, String.class);
        check(m != null, "resolveResource(String,String,String,String,String) declared");
    }
}
