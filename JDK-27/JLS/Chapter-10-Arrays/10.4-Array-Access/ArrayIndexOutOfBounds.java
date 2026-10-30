/*
 * @test
 * @summary JLS 10.4: an index less than zero or greater than or equal to the
 *          array length causes an ArrayIndexOutOfBoundsException at run time.
 * @run main ArrayIndexOutOfBounds
 */

public class ArrayIndexOutOfBounds {
    public static void main(String[] args) {
        int[] a = new int[3];
        try {
            a[3] = 1;
            throw new AssertionError("expected AIOOBE for a[3]");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            a[-1] = 1;
            throw new AssertionError("expected AIOOBE for a[-1]");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        System.out.println("PASS");
    }
}
