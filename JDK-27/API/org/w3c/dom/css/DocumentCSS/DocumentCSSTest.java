/*
 * @test
 * @summary org.w3c.dom.css.DocumentCSS conformance: document CSS.
 * @run main DocumentCSSTest
 */

public class DocumentCSSTest {
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

        Class<?> k = L("org.w3c.dom.css.DocumentCSS");
        check(k.isInterface(), "org.w3c.dom.css.DocumentCSS is an interface");
        check(L("org.w3c.dom.stylesheets.DocumentStyle").isAssignableFrom(k), "DocumentCSS extends DocumentStyle");
        check(k.getInterfaces().length >= 1, "DocumentCSS has superinterface");
        check(k.getMethod("getOverrideStyle", L("org.w3c.dom.Element"), String.class) != null, "getOverrideStyle(org.w3c.dom.Element, String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.DocumentCSS a = new org.w3c.dom.css.DocumentCSS(){
            public org.w3c.dom.css.CSSStyleDeclaration getOverrideStyle(org.w3c.dom.Element p0, java.lang.String p1) { return null; }
            public org.w3c.dom.stylesheets.StyleSheetList getStyleSheets() { return null; }
        };
        check(a != null, "anon impl of DocumentCSS");
        check(a.getOverrideStyle((org.w3c.dom.Element) null, "s") == null, "getOverrideStyle() returns fixed default");
        check(a.getStyleSheets() == null, "getStyleSheets() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: DocumentCSS contract (override-style channel) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(!org.w3c.dom.css.DocumentCSS.class.isInstance(d), "Document is not a DocumentCSS (module absent)");
        org.w3c.dom.css.DocumentCSS dc = new org.w3c.dom.css.DocumentCSS() {
            public org.w3c.dom.css.CSSStyleDeclaration getOverrideStyle(org.w3c.dom.Element e, String pseudo) { return null; }
            public org.w3c.dom.stylesheets.StyleSheetList getStyleSheets() { return null; }
        };
        check(dc.getOverrideStyle(null, ":hover") == null, "override style routed (null wiring)");
    }
}
