/*
 * @test
 * @summary JLS 10.2: a single array variable may hold references to arrays of
 *          different lengths, since length is not part of the array type.
 * @run main ArrayVariableDifferentLengths
 */

public class ArrayVariableDifferentLengths {
    public static void main(String[] args) {
        int[] v = new int[2];
        v = new int[5];
        if (v.length != 5) throw new AssertionError("reassigned length");
        System.out.println("PASS");
    }
}
