/*
 * @test
 * @summary org.w3c.dom.DOMImplementationSource conformance: reflection shape checks.
 * @run main DOMImplementationSourceTest
 */

import java.lang.reflect.*;

public class DOMImplementationSourceTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMImplementationSource");
        check(c != null, "org.w3c.dom.DOMImplementationSource loads");
        check(c.getName().equals("org.w3c.dom.DOMImplementationSource"), "binary name is org.w3c.dom.DOMImplementationSource");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMImplementationSource is public");
        check(c.isInterface(), "org.w3c.dom.DOMImplementationSource is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getDOMImplementation"), "declares getDOMImplementation");
        check(declares(c, "getDOMImplementationList"), "declares getDOMImplementationList");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMImplementationSource contract (registry as the JDK source) ----
    static void deepContract() throws Exception {
        org.w3c.dom.bootstrap.DOMImplementationRegistry reg = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance();
        // getDOMImplementation: first match semantics
        org.w3c.dom.DOMImplementation core = reg.getDOMImplementation("Core 3.0");
        check(core != null && core.hasFeature("Core", "3.0"), "getDOMImplementation(Core 3.0) returns a supporting implementation");
        check(reg.getDOMImplementation("XML 3.0") != null, "getDOMImplementation(XML 3.0) non-null");
        check(reg.getDOMImplementation("LS 3.0") != null, "getDOMImplementation(LS 3.0) non-null");
        check(reg.getDOMImplementation("bogus 1.0") == null, "getDOMImplementation(unknown) null");
        check(reg.getDOMImplementation("CSS 2.0") == null, "getDOMImplementation(CSS 2.0) null (no implementation)");
        check(reg.getDOMImplementation("HTML 2.0") == null, "getDOMImplementation(HTML 2.0) null");
        boolean npe = false;
        try { reg.getDOMImplementation(null); } catch (NullPointerException expected) { npe = true; }
        check(npe, "getDOMImplementation(null) -> NullPointerException");
        // list variant contract
        org.w3c.dom.DOMImplementationList l = reg.getDOMImplementationList("Core 3.0");
        check(l != null && l.item(0) == core, "getDOMImplementationList item(0) matches getDOMImplementation");
        // multi-feature queries: space-separated feature/version tokens
        check(reg.getDOMImplementation("Core 3.0 LS 3.0") != null, "combined feature query resolves");
        check(reg.getDOMImplementation("Core 3.0 bogus 1.0") == null, "combined query with unknown token fails");
        // interface facade: a user-supplied source must route both methods consistently
        final org.w3c.dom.DOMImplementation stub = new org.w3c.dom.DOMImplementation() {
            public boolean hasFeature(String feature, String version) { return "Stub".equals(feature) && "1.0".equals(version); }
            public org.w3c.dom.DocumentType createDocumentType(String q, String p, String s) { throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "stub"); }
            public org.w3c.dom.Document createDocument(String ns, String q, org.w3c.dom.DocumentType t) { throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "stub"); }
            public Object getFeature(String feature, String version) { return hasFeature(feature, version) ? this : null; }
        };
        org.w3c.dom.DOMImplementationSource src = new org.w3c.dom.DOMImplementationSource() {
            public org.w3c.dom.DOMImplementation getDOMImplementation(String features) {
                return "Stub 1.0".equals(features) ? stub : null;
            }
            public org.w3c.dom.DOMImplementationList getDOMImplementationList(String features) {
                final boolean hit = "Stub 1.0".equals(features);
                return new org.w3c.dom.DOMImplementationList() {
                    public org.w3c.dom.DOMImplementation item(int index) { return hit && index == 0 ? stub : null; }
                    public int getLength() { return hit ? 1 : 0; }
                };
            }
        };
        check(src.getDOMImplementation("Stub 1.0") == stub, "source facade routes single lookup");
        check(src.getDOMImplementation("other") == null, "source facade returns null for non-matching features");
        org.w3c.dom.DOMImplementationList sl = src.getDOMImplementationList("Stub 1.0");
        check(sl.getLength() == 1 && sl.item(0) == stub && sl.item(1) == null, "source facade routes list lookup");
        check(src.getDOMImplementationList("other").getLength() == 0, "source facade empty list for non-matching features");
    }
}
