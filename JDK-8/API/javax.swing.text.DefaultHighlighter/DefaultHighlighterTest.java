/*
 * @test
 * @summary javax.swing.text.DefaultHighlighter conformance: highlighter (headless-safe).
 * @run main DefaultHighlighterTest
 */

import javax.swing.text.DefaultHighlighter;

public class DefaultHighlighterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultHighlighter h = new DefaultHighlighter();
        check(h.getHighlights().length == 0, "no highlights");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
