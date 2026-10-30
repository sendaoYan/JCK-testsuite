/*
 * @test
 * @summary JLS 10.7: the public final field length may be positive or zero.
 * @run main ArrayLengthField
 */

public class ArrayLengthField {
    public static void main(String[] args) {
        int[] zero = new int[0];
        if (zero.length != 0) throw new AssertionError("zero length");

        int[] positive = new int[5];
        if (positive.length != 5) throw new AssertionError("positive length");
        System.out.println("PASS");
    }
}
