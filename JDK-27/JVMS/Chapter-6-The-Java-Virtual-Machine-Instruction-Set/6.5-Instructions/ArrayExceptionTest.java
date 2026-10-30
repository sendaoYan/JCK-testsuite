/*
 * @test
 * @summary JVMS 6.5: array instructions throw ArrayIndexOutOfBoundsException,
 *          NullPointerException, and NegativeArraySizeException in the
 *          specified situations.
 * @compile ArrayOps.jasm
 * @run main ArrayExceptionTest
 */

public class ArrayExceptionTest {
    public static void main(String[] args) {
        int[] a = ArrayOps.newIntArray(3);

        try {
            ArrayOps.iaload(a, 3);
            throw new AssertionError("iaload out of bounds did not throw");
        } catch (ArrayIndexOutOfBoundsException e) { }

        try {
            ArrayOps.iastore(a, -1, 1);
            throw new AssertionError("iastore out of bounds did not throw");
        } catch (ArrayIndexOutOfBoundsException e) { }

        try {
            ArrayOps.iaload(null, 0);
            throw new AssertionError("iaload null did not throw");
        } catch (NullPointerException e) { }

        try {
            ArrayOps.newIntArray(-1);
            throw new AssertionError("newarray negative did not throw");
        } catch (NegativeArraySizeException e) { }

        System.out.println("PASS");
    }
}
