/*
 * @test
 * @summary javax.swing.text.DefaultCaret conformance: caret (headless-safe).
 * @run main DefaultCaretTest
 */

import javax.swing.text.DefaultCaret;

public class DefaultCaretTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultCaret c = new DefaultCaret();
        check(c.getDot() == 0 && c.getMark() == 0, "dot/mark");
        check(!c.isVisible(), "not visible");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
