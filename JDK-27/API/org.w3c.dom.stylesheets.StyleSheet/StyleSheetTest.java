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

    public static void main(String[] args) throws Exception {
        check(StyleSheet.class.isInterface(), "StyleSheet is an interface");
        check(StyleSheet.class.getMethod("getType") != null, "getType()");
        check(StyleSheet.class.getMethod("getDisabled")
                .getReturnType() == boolean.class, "getDisabled() returns boolean");
        check(StyleSheet.class.getMethod("getOwnerNode") != null, "getOwnerNode()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
