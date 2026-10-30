/*
 * @test
 * @summary JLS 10.6: nested array initializers construct and initialize
 *          multidimensional arrays by recursive application of the algorithm.
 * @run main ArrayInitializer
 */

public class ArrayInitializer {
    public static void main(String[] args) {
        int[][] ia = { { 1, 2 }, { 3, 4, 5 } };
        if (ia.length != 2) throw new AssertionError("outer length");
        if (ia[0].length != 2 || ia[1].length != 3) throw new AssertionError("inner lengths");
        if (ia[0][1] != 2 || ia[1][2] != 5) throw new AssertionError("values");
        System.out.println("PASS");
    }
}
