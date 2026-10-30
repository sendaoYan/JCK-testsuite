/*
 * @test
 * @summary JLS 10.6 (Java SE 8): array initializers may be used in declarations,
 *          and nested initializers create multi-dimensional arrays.
 * @run main ArrayInitializerTest
 */

public class ArrayInitializerTest {
    public static void main(String[] args) {
        int[] a = {1, 2, 3};
        if (a.length != 3 || a[0] != 1 || a[2] != 3) throw new AssertionError("array initializer");

        String[] s = {"x", "y"};
        if (s.length != 2 || !s[1].equals("y")) throw new AssertionError("string initializer");

        int[][] m = {{1, 2}, {3, 4, 5}};
        if (m.length != 2 || m[1].length != 3 || m[1][2] != 5) throw new AssertionError("nested initializer");

        int[] empty = {};
        if (empty.length != 0) throw new AssertionError("empty initializer");

        System.out.println("PASS");
    }
}
