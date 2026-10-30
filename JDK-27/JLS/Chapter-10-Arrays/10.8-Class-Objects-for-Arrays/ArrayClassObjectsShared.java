/*
 * @test
 * @summary JLS 10.8 (Example 10.8-2): all arrays with the same component type
 *          share one Class object, even when their lengths differ.
 * @run main ArrayClassObjectsShared
 */

public class ArrayClassObjectsShared {
    public static void main(String[] args) {
        int[] ia = new int[3];
        int[] ib = new int[6];
        if (ia == ib) throw new AssertionError("distinct arrays");
        if (ia.getClass() != ib.getClass())
            throw new AssertionError("Class objects should be shared");
        System.out.println("PASS");
    }
}
