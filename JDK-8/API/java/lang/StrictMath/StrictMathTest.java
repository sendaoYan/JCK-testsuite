/*
 * @test
 * @summary java.lang.StrictMath API conformance: abs/sqrt/pow/min/max/floor/ceil/round and
 *          the E/PI constants.
 * @run main StrictMathTest
 */

public class StrictMathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStrictMath() {
        check(StrictMath.abs(-5) == 5 && StrictMath.abs(5) == 5, "abs");
        check(StrictMath.sqrt(16.0) == 4.0, "sqrt");
        check(StrictMath.pow(2.0, 10.0) == 1024.0, "pow");
        check(StrictMath.min(3, 5) == 3 && StrictMath.max(3, 5) == 5, "min/max");
        check(StrictMath.floor(2.7) == 2.0 && StrictMath.ceil(2.3) == 3.0, "floor/ceil");
        check(StrictMath.round(2.6f) == 3, "round float");
        check(StrictMath.round(2.4) == 2, "round double");
        check(StrictMath.E == Math.E && StrictMath.PI == Math.PI, "E/PI");
    }

    public static void main(String[] args) {
        testStrictMath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
