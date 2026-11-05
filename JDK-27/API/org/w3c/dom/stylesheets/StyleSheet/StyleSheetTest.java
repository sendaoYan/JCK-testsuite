/*
 * @test
 * @summary org.w3c.dom.stylesheets.StyleSheet conformance: interface and methods.
 * @run main StyleSheetTest
 */

import org.w3c.dom.stylesheets.StyleSheet;

public class StyleSheetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(StyleSheet.class.isInterface(), "StyleSheet is an interface");
        check(StyleSheet.class.getMethod("getType") != null, "getType()");
        check(StyleSheet.class.getMethod("getDisabled")
                .getReturnType() == boolean.class, "getDisabled() returns boolean");
        check(StyleSheet.class.getMethod("getOwnerNode") != null, "getOwnerNode()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.stylesheets.StyleSheet a = new org.w3c.dom.stylesheets.StyleSheet(){
            public java.lang.String getType() { return "s"; }
            public boolean getDisabled() { return true; }
            public void setDisabled(boolean p0) { }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return null; }
            public java.lang.String getHref() { return "s"; }
            public java.lang.String getTitle() { return "s"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        check(a != null, "anon impl of StyleSheet");
        check("s".equals(a.getType()), "getType() returns fixed default");
        check(a.getDisabled() == true, "getDisabled() returns fixed default");
        a.setDisabled(true);
        check(a.getOwnerNode() == null, "getOwnerNode() returns fixed default");
        check(a.getParentStyleSheet() == null, "getParentStyleSheet() returns fixed default");
        check("s".equals(a.getHref()), "getHref() returns fixed default");
        check("s".equals(a.getTitle()), "getTitle() returns fixed default");
        check(a.getMedia() == null, "getMedia() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: StyleSheet contract (metadata + disable toggle) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        final boolean[] disabled = new boolean[]{false};
        final org.w3c.dom.stylesheets.StyleSheet self = new org.w3c.dom.stylesheets.StyleSheet() {
            public String getType() { return "text/css"; }
            public boolean getDisabled() { return disabled[0]; }
            public void setDisabled(boolean b) { disabled[0] = b; }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return this; }
            public String getHref() { return "base.css"; }
            public String getTitle() { return "base"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        check("text/css".equals(self.getType()) && "base.css".equals(self.getHref()) && "base".equals(self.getTitle()), "type/href/title metadata");
        check(!self.getDisabled(), "starts enabled");
        self.setDisabled(true);
        check(self.getDisabled(), "setDisabled toggles");
        check(self.getParentStyleSheet() == self && self.getOwnerNode() == null, "parent/owner wiring");
    }
}
