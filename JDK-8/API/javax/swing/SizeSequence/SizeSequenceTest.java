/*
 * @test
 * @summary javax.swing.SizeSequence conformance: sizing (headless-safe).
 * @run main SizeSequenceTest
 */

import javax.swing.SizeSequence;

public class SizeSequenceTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        SizeSequence s = new SizeSequence(new int[] {10, 20, 30});
        check(s.getSizes().length == 3, "length");
        check(s.getSize(0) == 10 && s.getSize(1) == 20 && s.getSize(2) == 30, "sizes");
        check(s.getIndex(5) == 0, "getIndex 0");
        check(s.getIndex(15) == 1, "getIndex 1");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
