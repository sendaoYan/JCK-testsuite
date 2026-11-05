/*
 * @test
 * @summary org.w3c.dom.html.HTMLDOMImplementation conformance: DOMImplementation subtype.
 * @run main HTMLDOMImplementationTest
 */

public class HTMLDOMImplementationTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.html.HTMLDOMImplementation");
        check(k.isInterface(), "org.w3c.dom.html.HTMLDOMImplementation is an interface");
        check(L("org.w3c.dom.DOMImplementation").isAssignableFrom(k), "HTMLDOMImplementation extends DOMImplementation");
        check(k.getInterfaces().length >= 1, "HTMLDOMImplementation has superinterface");
        check(k.getMethod("createHTMLDocument", String.class) != null, "createHTMLDocument(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.html.HTMLDOMImplementation a = new org.w3c.dom.html.HTMLDOMImplementation(){
            public org.w3c.dom.html.HTMLDocument createHTMLDocument(java.lang.String p0) { return null; }
            public boolean hasFeature(java.lang.String p0, java.lang.String p1) { return true; }
            public org.w3c.dom.DocumentType createDocumentType(java.lang.String p0, java.lang.String p1, java.lang.String p2) { return null; }
            public org.w3c.dom.Document createDocument(java.lang.String p0, java.lang.String p1, org.w3c.dom.DocumentType p2) { return null; }
            public java.lang.Object getFeature(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of HTMLDOMImplementation");
        check(a.createHTMLDocument("s") == null, "createHTMLDocument() returns fixed default");
        check(a.hasFeature("s", "s") == true, "hasFeature() returns fixed default");
        check(a.createDocumentType("s", "s", "s") == null, "createDocumentType() returns fixed default");
        check(a.createDocument("s", "s", (org.w3c.dom.DocumentType) null) == null, "createDocument() returns fixed default");
        check(a.getFeature("s", "s") == null, "getFeature() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
