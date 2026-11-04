/*
 * @test
 * @summary javax.swing.text.Segment conformance: segment (headless-safe).
 * @run main SegmentTest
 */

import javax.swing.text.Segment;

public class SegmentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        Segment s = new Segment();
        s.array = new char[] {'a', 'b', 'c'};
        s.count = 3;
        check(s.length() == 3, "length");
        check("abc".equals(s.toString()), "toString");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
