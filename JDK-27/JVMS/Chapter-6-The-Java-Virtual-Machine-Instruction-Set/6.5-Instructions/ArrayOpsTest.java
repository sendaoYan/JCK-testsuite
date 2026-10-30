/*
 * @test
 * @summary JVMS 6.5: array creation and access instructions newarray,
 *          arraylength, iaload, and iastore.
 * @compile ArrayOps.jasm
 * @run main ArrayOpsTest
 */

public class ArrayOpsTest {
    public static void main(String[] args) {
        int[] a = ArrayOps.newIntArray(5);
        if (ArrayOps.arrayLength(a) != 5) throw new AssertionError("arraylength");

        ArrayOps.iastore(a, 2, 42);
        if (ArrayOps.iaload(a, 2) != 42) throw new AssertionError("iastore/iaload");

        System.out.println("PASS");
    }
}
