/*
 * @test
 * @summary javax.swing.text.GapContent conformance: content (headless-safe).
 * @run main GapContentTest
 */

import javax.swing.text.GapContent;

public class GapContentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        GapContent g = new GapContent();
        check(g.length() == 1, "initial length");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
