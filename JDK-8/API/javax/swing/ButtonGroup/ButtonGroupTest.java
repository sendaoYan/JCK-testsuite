/*
 * @test
 * @summary javax.swing.ButtonGroup conformance: button management (headless-safe).
 * @run main ButtonGroupTest
 */

import javax.swing.ButtonGroup;

public class ButtonGroupTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        ButtonGroup g = new ButtonGroup();
        check(g.getButtonCount() == 0, "no buttons");
        check(g.getSelection() == null, "no selection");
        check(g.getElements() != null, "elements");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
