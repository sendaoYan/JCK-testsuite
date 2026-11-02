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

    public static void main(String[] args) throws Exception {
        check(StyleSheetList.class.isInterface(), "StyleSheetList is an interface");
        check(StyleSheetList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(StyleSheetList.class.getMethod("item", int.class)
                .getReturnType() == StyleSheet.class, "item(int) returns StyleSheet");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
