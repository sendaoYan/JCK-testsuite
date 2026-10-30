/*
 * @test
 * @summary JLS 10.1: an array's length is not part of its type; arrays of the
 *          same component type but different lengths share one Class object.
 * @run main ArrayLengthNotPartOfType
 */

public class ArrayLengthNotPartOfType {
    public static void main(String[] args) {
        int[] a = new int[3];
        int[] b = new int[5];
        if (a.getClass() != b.getClass())
            throw new AssertionError("length should not affect array type");
        if (a.length == b.length) throw new AssertionError("lengths differ");
        System.out.println("PASS");
    }
}
