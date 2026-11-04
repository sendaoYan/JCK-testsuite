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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
