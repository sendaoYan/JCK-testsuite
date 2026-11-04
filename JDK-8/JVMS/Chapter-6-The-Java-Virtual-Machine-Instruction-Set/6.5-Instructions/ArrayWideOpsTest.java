/*
 * @test
 * @summary JVMS 6.5: laload/lastore, faload/fastore, and daload/dastore load
 *          and store the long/float/double component types of an array.
 * @compile ArrayWideOps.jasm
 * @run main ArrayWideOpsTest
 */

public class ArrayWideOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        long[] la = { 1L, 2L, 3L };
        check(ArrayWideOps.laload(la, 1) == 2L, "laload");
        ArrayWideOps.lastore(la, 0, 9L);
        check(la[0] == 9L, "lastore");

        float[] fa = { 1.5f, 2.5f };
        check(ArrayWideOps.faload(fa, 1) == 2.5f, "faload");
        ArrayWideOps.fastore(fa, 0, 7.5f);
        check(fa[0] == 7.5f, "fastore");

        double[] da = { 1.5, 2.5 };
        check(ArrayWideOps.daload(da, 1) == 2.5, "daload");
        ArrayWideOps.dastore(da, 0, 9.5);
        check(da[0] == 9.5, "dastore");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
