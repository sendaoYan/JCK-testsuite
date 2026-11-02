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

    public static void main(String[] args) throws Exception {
        check(DocumentStyle.class.isInterface(), "DocumentStyle is an interface");
        check(DocumentStyle.class.getMethod("getStyleSheets")
                .getReturnType() == StyleSheetList.class,
              "getStyleSheets() returns StyleSheetList");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
