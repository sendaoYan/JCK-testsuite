/*
 * @test
 * @summary JLS 8.8.1: a constructor whose last formal parameter is a variable
 *          arity parameter is a variable arity constructor.
 * @run main VariableArityConstructor
 */

public class VariableArityConstructor {
    int[] a;
    VariableArityConstructor(int... xs) { a = xs; }

    public static void main(String[] args) {
        VariableArityConstructor c = new VariableArityConstructor(1, 2, 3);
        if (c.a.length != 3 || c.a[0] != 1 || c.a[2] != 3)
            throw new AssertionError("varargs constructor");
        System.out.println("PASS");
    }
}
