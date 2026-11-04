/*
 * @test
 * @summary javax.swing.text.StyleContext conformance: style context (headless-safe).
 * @run main StyleContextTest
 */

import javax.swing.text.StyleContext;

public class StyleContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        StyleContext c = new StyleContext();
        check(c.getStyle("nope") == null, "no style");
        check(c.getStyleNames() != null, "style names");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
