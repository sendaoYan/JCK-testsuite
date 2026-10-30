/*
 * @test
 * @summary JLS 10.7 (Example 10.7-2): cloning a multidimensional array is shallow;
 *          subarrays are shared.
 * @run main ArrayCloneShallow
 */

public class ArrayCloneShallow {
    public static void main(String[] args) {
        int[][] ia = { { 1, 2 }, null };
        int[][] ja = ia.clone();
        if (ia == ja) throw new AssertionError("clone should be distinct");
        if (ia[0] != ja[0]) throw new AssertionError("subarray not shared");
        if (ia[1] != ja[1]) throw new AssertionError("null subarray not shared");
        System.out.println("PASS");
    }
}
