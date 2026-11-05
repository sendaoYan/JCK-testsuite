/*
 * @test
 * @summary org.w3c.dom.stylesheets.DocumentStyle conformance: interface and getStyleSheets.
 * @run main DocumentStyleTest
 */

import org.w3c.dom.stylesheets.DocumentStyle;
import org.w3c.dom.stylesheets.StyleSheetList;

public class DocumentStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentStyle.class.isInterface(), "DocumentStyle is an interface");
        check(DocumentStyle.class.getMethod("getStyleSheets")
                .getReturnType() == StyleSheetList.class,
              "getStyleSheets() returns StyleSheetList");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.stylesheets.DocumentStyle a = new org.w3c.dom.stylesheets.DocumentStyle(){
            public org.w3c.dom.stylesheets.StyleSheetList getStyleSheets() { return null; }
        };
        check(a != null, "anon impl of DocumentStyle");
        check(a.getStyleSheets() == null, "getStyleSheets() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: DocumentStyle contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(!org.w3c.dom.stylesheets.DocumentStyle.class.isInstance(d), "Document is not a DocumentStyle (module absent)");
        org.w3c.dom.stylesheets.StyleSheetList empty = new org.w3c.dom.stylesheets.StyleSheetList() {
            public int getLength() { return 0; }
            public org.w3c.dom.stylesheets.StyleSheet item(int i) { return null; }
        };
        org.w3c.dom.stylesheets.DocumentStyle ds = new org.w3c.dom.stylesheets.DocumentStyle() {
            public org.w3c.dom.stylesheets.StyleSheetList getStyleSheets() { return empty; }
        };
        check(ds.getStyleSheets() == empty && empty.getLength() == 0 && empty.item(0) == null, "styleSheets list routed");
    }
}
