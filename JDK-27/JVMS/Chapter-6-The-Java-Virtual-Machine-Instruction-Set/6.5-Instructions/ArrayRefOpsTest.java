/*
 * @test
 * @summary JVMS 6.5: reference array creation and access anewarray, aaload,
 *          and aastore.
 * @compile ArrayRefOps.jasm
 * @run main ArrayRefOpsTest
 */

public class ArrayRefOpsTest {
    public static void main(String[] args) {
        String[] a = ArrayRefOps.mk(3);
        if (a.length != 3) throw new AssertionError("anewarray length");
        if (ArrayRefOps.get(a, 0) != null) throw new AssertionError("anewarray default null");

        ArrayRefOps.set(a, 1, "x");
        if (!"x".equals(ArrayRefOps.get(a, 1))) throw new AssertionError("aastore/aaload");

        System.out.println("PASS");
    }
}
