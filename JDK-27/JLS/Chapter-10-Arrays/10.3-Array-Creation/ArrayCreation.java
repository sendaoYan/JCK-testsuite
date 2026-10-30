/*
 * @test
 * @summary JLS 10.3: an array creation expression allocates an array whose length
 *          is available as the final instance variable length; components get
 *          their default values.
 * @run main ArrayCreation
 */

public class ArrayCreation {
    public static void main(String[] args) {
        int[] a = new int[10];
        if (a.length != 10) throw new AssertionError("int length");
        if (a[0] != 0) throw new AssertionError("int default value");

        String[] s = new String[3];
        if (s.length != 3) throw new AssertionError("String length");
        if (s[0] != null) throw new AssertionError("reference default value");

        int[] empty = new int[0];
        if (empty.length != 0) throw new AssertionError("zero length");
        System.out.println("PASS");
    }
}
