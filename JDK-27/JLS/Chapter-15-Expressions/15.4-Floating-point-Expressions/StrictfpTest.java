/*
 * @test
 * @summary JLS 15.4: within a strictfp method every intermediate floating-point
 *          result is rounded to the value set exactly, so the result is
 *          deterministic and identical to the non-strictfp IEEE 754 double
 *          result for these inputs.
 * @run main StrictfpTest
 */

public class StrictfpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    strictfp double compute() {
        return 0.1 + 0.2;
    }

    public static void main(String[] args) {
        StrictfpTest t = new StrictfpTest();
        check(t.compute() == 0.30000000000000004, "strictfp double addition is IEEE 754 exact");
        check(t.compute() == (0.1 + 0.2), "strictfp matches ordinary double arithmetic here");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
