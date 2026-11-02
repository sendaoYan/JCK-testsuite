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

    public static void main(String[] args) throws Exception {
        check(LinkStyle.class.isInterface(), "LinkStyle is an interface");
        check(LinkStyle.class.getMethod("getSheet")
                .getReturnType() == StyleSheet.class, "getSheet() returns StyleSheet");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
