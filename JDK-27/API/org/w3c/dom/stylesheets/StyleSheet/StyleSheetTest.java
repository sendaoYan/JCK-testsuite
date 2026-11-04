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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
