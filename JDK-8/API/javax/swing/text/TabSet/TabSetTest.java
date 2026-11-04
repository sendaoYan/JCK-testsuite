/*
 * @test
 * @summary javax.swing.text.TabSet conformance: tab set (headless-safe).
 * @run main TabSetTest
 */

import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class TabSetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        TabSet s = new TabSet(new TabStop[] {new TabStop(10f), new TabStop(20f)});
        check(s.getTabCount() == 2, "count");
        check(s.getTab(0).getPosition() == 10f, "tab 0");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
