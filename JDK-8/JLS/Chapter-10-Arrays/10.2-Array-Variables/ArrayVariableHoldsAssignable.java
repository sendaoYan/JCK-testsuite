/*
 * @test
 * @summary JLS 10.2 (Java SE 8): a variable of array type A[] can hold any B[]
 *          where B is assignable to A; this may later cause an
 *          ArrayStoreException on assignment (see 10.5).
 * @run main ArrayVariableHoldsAssignable
 */

public class ArrayVariableHoldsAssignable {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class A {}
    static class B extends A {}

    public static void main(String[] args) {
        Object[] oa = new String[3];
        check(oa.length == 3, "Object[] holds String[]");

        int[] ai = new int[1];
        ai = new int[7];
        check(ai.length == 7, "single variable holds different lengths");

        A[] aa = new B[2];
        check(aa.length == 2, "A[] holds B[]");
        boolean threw = false;
        try {
            aa[0] = new A();
        } catch (ArrayStoreException expected) {
            threw = true;
        }
        check(threw, "ArrayStoreException on A into B[]");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
