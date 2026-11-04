/*
 * @test
 * @summary JLS 10.7 (Java SE 8, Example 10.7-2): cloning a multidimensional array
 *          is shallow; subarrays are shared.
 * @run main ArrayCloneShallow
 */

public class ArrayCloneShallow {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int[][] ia = { { 1, 2 }, null };
        int[][] ja = ia.clone();
        check(ia != ja, "clone should be distinct");
        check(ia[0] == ja[0], "subarray shared");
        check(ia[1] == ja[1], "null subarray shared");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
