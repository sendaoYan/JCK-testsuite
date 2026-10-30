/*
 * @test
 * @summary JLS 10.7 (Example 10.7-1): an array's clone method returns an array of
 *          the same type; the clone is a distinct array whose components are
 *          different variables.
 * @run main ArrayClone
 */

public class ArrayClone {
    public static void main(String[] args) {
        int[] ia1 = { 1, 2 };
        int[] ia2 = ia1.clone();
        if (ia1 == ia2) throw new AssertionError("clone should be distinct");
        ia1[1]++;
        if (ia2[1] != 2) throw new AssertionError("components are different variables");
        System.out.println("PASS");
    }
}
