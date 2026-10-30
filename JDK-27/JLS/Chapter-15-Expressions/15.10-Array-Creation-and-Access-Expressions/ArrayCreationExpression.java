/*
 * @test
 * @summary JLS 15.10: an array creation expression with several dims creates a
 *          single multidimensional array whose component sub-arrays are distinct;
 *          an array initializer creates and initializes the array.
 * @run main ArrayCreationExpression
 */

public class ArrayCreationExpression {
    public static void main(String[] args) {
        int[][] m = new int[3][3];   // a single multidimensional array creation
        if (m.length != 3 || m[0].length != 3)
            throw new AssertionError("multidimensional array");
        m[0][0] = 1;
        if (m[1][0] != 0) throw new AssertionError("sub-arrays must be distinct");

        int[] a = new int[]{1, 2, 3};
        if (a.length != 3 || a[2] != 3) throw new AssertionError("array initializer");

        System.out.println("PASS");
    }
}
