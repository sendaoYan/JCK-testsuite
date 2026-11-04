/*
 * @test
 * @summary javax.swing.text.TabStop conformance: tab stop (headless-safe).
 * @run main TabStopTest
 */

import javax.swing.text.TabStop;

public class TabStopTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        TabStop t = new TabStop(10.5f);
        check(t.getPosition() == 10.5f, "position");
        check(t.getAlignment() == TabStop.ALIGN_LEFT, "alignment");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
