/*
 * @test
 * @summary org.w3c.dom.stylesheets.StyleSheetList conformance: interface and methods.
 * @run main StyleSheetListTest
 */

import org.w3c.dom.stylesheets.StyleSheet;
import org.w3c.dom.stylesheets.StyleSheetList;

public class StyleSheetListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(StyleSheetList.class.isInterface(), "StyleSheetList is an interface");
        check(StyleSheetList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(StyleSheetList.class.getMethod("item", int.class)
                .getReturnType() == StyleSheet.class, "item(int) returns StyleSheet");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.stylesheets.StyleSheetList a = new org.w3c.dom.stylesheets.StyleSheetList(){
            public int getLength() { return 3; }
            public org.w3c.dom.stylesheets.StyleSheet item(int p0) { return null; }
        };
        check(a != null, "anon impl of StyleSheetList");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.item(3) == null, "item() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
