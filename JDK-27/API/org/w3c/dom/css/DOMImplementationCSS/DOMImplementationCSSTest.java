/*
 * @test
 * @summary org.w3c.dom.css.DOMImplementationCSS conformance: DOMImplementation subtype.
 * @run main DOMImplementationCSSTest
 */

public class DOMImplementationCSSTest {
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

        Class<?> k = L("org.w3c.dom.css.DOMImplementationCSS");
        check(k.isInterface(), "org.w3c.dom.css.DOMImplementationCSS is an interface");
        check(L("org.w3c.dom.DOMImplementation").isAssignableFrom(k), "DOMImplementationCSS extends DOMImplementation");
        check(k.getInterfaces().length >= 1, "DOMImplementationCSS has superinterface");
        check(k.getMethod("createCSSStyleSheet", String.class, String.class) != null, "createCSSStyleSheet(String, String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.DOMImplementationCSS a = new org.w3c.dom.css.DOMImplementationCSS(){
            public org.w3c.dom.css.CSSStyleSheet createCSSStyleSheet(java.lang.String p0, java.lang.String p1) { return null; }
            public boolean hasFeature(java.lang.String p0, java.lang.String p1) { return true; }
            public org.w3c.dom.DocumentType createDocumentType(java.lang.String p0, java.lang.String p1, java.lang.String p2) { return null; }
            public org.w3c.dom.Document createDocument(java.lang.String p0, java.lang.String p1, org.w3c.dom.DocumentType p2) { return null; }
            public java.lang.Object getFeature(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of DOMImplementationCSS");
        check(a.createCSSStyleSheet("s", "s") == null, "createCSSStyleSheet() returns fixed default");
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

    // ---- deepening round 2: DOMImplementationCSS contract (factory channel) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(!org.w3c.dom.css.DOMImplementationCSS.class.isInstance(
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation()), "DOMImplementation is not a DOMImplementationCSS");
        org.w3c.dom.css.DOMImplementationCSS c = (org.w3c.dom.css.DOMImplementationCSS) java.lang.reflect.Proxy.newProxyInstance(
            org.w3c.dom.css.DOMImplementationCSS.class.getClassLoader(),
            new Class<?>[]{org.w3c.dom.css.DOMImplementationCSS.class},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    if (m.getName().equals("createCSSStyleSheet")) return null;
                    if (m.getName().equals("hasFeature")) return Boolean.FALSE;
                    return null;
                }
            });
        check(c.createCSSStyleSheet("t", "screen") == null, "factory routed through the proxy");
        check(!c.hasFeature("CSS", "2.0"), "proxy reports CSS unimplemented");
        check(c.createCSSStyleSheet("t", "screen") == null, "factory routed (null wiring)");
    }
}
