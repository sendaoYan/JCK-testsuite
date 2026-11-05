/*
 * @test
 * @summary org.w3c.dom.DOMImplementationSource conformance: interface and methods.
 * @run main DOMImplementationSourceTest
 */

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;

public class DOMImplementationSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMImplementationSource.class.isInterface(),
              "DOMImplementationSource is an interface");
        check(DOMImplementationSource.class.getMethod("getDOMImplementation", String.class)
                .getReturnType() == DOMImplementation.class,
              "getDOMImplementation(String) returns DOMImplementation");
        check(DOMImplementationSource.class.getMethod("getDOMImplementationList", String.class)
                .getReturnType() == DOMImplementationList.class,
              "getDOMImplementationList(String) returns DOMImplementationList");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMImplementationSource a = new org.w3c.dom.DOMImplementationSource(){
            public org.w3c.dom.DOMImplementation getDOMImplementation(java.lang.String p0) { return null; }
            public org.w3c.dom.DOMImplementationList getDOMImplementationList(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of DOMImplementationSource");
        check(a.getDOMImplementation("s") == null, "getDOMImplementation() returns fixed default");
        check(a.getDOMImplementationList("s") == null, "getDOMImplementationList() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
