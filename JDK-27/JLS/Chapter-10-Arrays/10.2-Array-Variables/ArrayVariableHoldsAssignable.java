/*
 * @test
 * @summary JLS 10.2: a variable of array type A[] (A a reference type) can hold a
 *          reference to any array type B[] where B is assignable to A; this may
 *          later cause an ArrayStoreException on assignment (see 10.5).
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
        // String is assignable to Object, so String[] can be held by Object[].
        Object[] oa = new String[3];
        check(oa.length == 3, "Object[] holds String[]");

        // A single variable can hold arrays of different lengths.
        int[] ai = new int[1];
        check(ai.length == 1, "int[] length 1");
        ai = new int[7];
        check(ai.length == 7, "int[] length 7 after reassignment");

        // B[] can be held by A[] because B is assignable to A.
        A[] aa = new B[2];
        check(aa.length == 2, "A[] holds B[]");

        // The runtime component type is retained: storing a plain A now throws.
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
