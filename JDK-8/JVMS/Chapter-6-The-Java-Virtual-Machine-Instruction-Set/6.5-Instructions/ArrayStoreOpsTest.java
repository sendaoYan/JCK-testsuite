/*
 * @test
 * @summary JVMS 6.5: anewarray and multianewarray throw
 *          NegativeArraySizeException for a negative length; arraylength throws
 *          NullPointerException on a null array; aastore throws
 *          ArrayStoreException when the value is not assignable to the array's
 *          component type.
 * @compile ArrayStoreOps.jasm
 * @run main ArrayStoreOpsTest
 */

public class ArrayStoreOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        try {
            ArrayStoreOps.anewarrayNeg(-1);
            throw new AssertionError("anewarray negative did not throw");
        } catch (NegativeArraySizeException e) { }
        check(true, "anewarray negative length");

        try {
            ArrayStoreOps.multiNeg(-1, 3);
            throw new AssertionError("multianewarray negative (dim 0) did not throw");
        } catch (NegativeArraySizeException e) { }
        check(true, "multianewarray negative first dimension");

        try {
            ArrayStoreOps.multiNeg(2, -1);
            throw new AssertionError("multianewarray negative (dim 1) did not throw");
        } catch (NegativeArraySizeException e) { }
        check(true, "multianewarray negative second dimension");

        try {
            ArrayStoreOps.arrayLengthNull();
            throw new AssertionError("arraylength null did not throw");
        } catch (NullPointerException e) { }
        check(true, "arraylength null");

        String[] s = new String[1];
        Object[] a = s;
        try {
            ArrayStoreOps.aastoreObj(a, new Integer(1));
            throw new AssertionError("aastore wrong type did not throw");
        } catch (ArrayStoreException e) { }
        check(true, "aastore ArrayStoreException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
