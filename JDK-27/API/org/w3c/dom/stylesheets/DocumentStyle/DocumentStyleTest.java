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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
