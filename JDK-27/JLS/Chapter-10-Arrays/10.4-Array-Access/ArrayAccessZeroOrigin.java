/*
 * @test
 * @summary JLS 10.4: arrays are 0-origin; an array of length n is indexed by the
 *          integers 0 through n-1.
 * @run main ArrayAccessZeroOrigin
 */

public class ArrayAccessZeroOrigin {
    public static void main(String[] args) {
        int[] a = new int[3];
        a[0] = 10;
        a[2] = 30;
        if (a[0] != 10 || a[1] != 0 || a[2] != 30) throw new AssertionError("zero origin");
        System.out.println("PASS");
    }
}
