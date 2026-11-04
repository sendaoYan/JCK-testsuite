/*
 * @test
 * @summary JLS 15.10.1 (Java SE 8): if the value of a dimension expression of an
 *          array creation expression is less than zero, a
 *          NegativeArraySizeException is thrown at run time.
 * @run main NegativeArraySize
 */

public class NegativeArraySize {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(new int[0].length == 0, "zero-length array");
        check(new int[10].length == 10, "length 10");
        try {
            int[] x = new int[-1];
            throw new AssertionError("NegativeArraySizeException expected for -1");
        } catch (NegativeArraySizeException expected) {
            passed++;
        }
        try {
            int[] x = new int[Integer.MIN_VALUE];
            throw new AssertionError("NegativeArraySizeException expected for MIN_VALUE");
        } catch (NegativeArraySizeException expected) {
            passed++;
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
