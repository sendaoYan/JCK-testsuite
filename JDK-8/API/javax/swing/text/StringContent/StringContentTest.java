/*
 * @test
 * @summary javax.swing.text.StringContent conformance: content (headless-safe).
 * @run main StringContentTest
 */

import javax.swing.text.StringContent;

public class StringContentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        StringContent s = new StringContent();
        check(s.length() == 1, "initial length");
        s.insertString(0, "hi");
        check(s.length() == 3, "length after insert");
        check("hi".equals(s.getString(0, 2)), "getString");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
