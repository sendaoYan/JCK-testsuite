/*
 * @test
 * @summary javax.swing.border.TitledBorder conformance: title (headless-safe).
 * @run main TitledBorderTest
 */

import javax.swing.border.TitledBorder;

public class TitledBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        TitledBorder b = new TitledBorder("Hello");
        check("Hello".equals(b.getTitle()), "title");
        check(b.getBorder() != null, "has default border");
        check(b.getTitlePosition() == TitledBorder.DEFAULT_POSITION, "position");
        check(b.getTitleJustification() == TitledBorder.LEADING, "justification");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
