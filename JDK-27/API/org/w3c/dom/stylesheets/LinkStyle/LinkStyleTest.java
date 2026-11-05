/*
 * @test
 * @summary org.w3c.dom.stylesheets.LinkStyle conformance: interface and getSheet.
 * @run main LinkStyleTest
 */

import org.w3c.dom.stylesheets.LinkStyle;
import org.w3c.dom.stylesheets.StyleSheet;

public class LinkStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(LinkStyle.class.isInterface(), "LinkStyle is an interface");
        check(LinkStyle.class.getMethod("getSheet")
                .getReturnType() == StyleSheet.class, "getSheet() returns StyleSheet");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.stylesheets.LinkStyle a = new org.w3c.dom.stylesheets.LinkStyle(){
            public org.w3c.dom.stylesheets.StyleSheet getSheet() { return null; }
        };
        check(a != null, "anon impl of LinkStyle");
        check(a.getSheet() == null, "getSheet() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: LinkStyle contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        org.w3c.dom.stylesheets.StyleSheet sheet = new org.w3c.dom.stylesheets.StyleSheet() {
            public String getType() { return "text/css"; }
            public boolean getDisabled() { return false; }
            public void setDisabled(boolean b) { }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return null; }
            public String getHref() { return null; }
            public String getTitle() { return "linked"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        org.w3c.dom.stylesheets.LinkStyle ls = new org.w3c.dom.stylesheets.LinkStyle() {
            public org.w3c.dom.stylesheets.StyleSheet getSheet() { return sheet; }
        };
        check(ls.getSheet() == sheet && "linked".equals(ls.getSheet().getTitle()), "owning node exposes its sheet");
    }
}
