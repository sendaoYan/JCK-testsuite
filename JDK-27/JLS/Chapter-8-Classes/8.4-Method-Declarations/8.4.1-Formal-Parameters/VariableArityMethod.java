/*
 * @test
 * @summary JLS 8.4.1: a variable arity method may be invoked with more actual
 *          argument expressions than formal parameters; the extra arguments are
 *          collected into an array.
 * @run main VariableArityMethod
 */

public class VariableArityMethod {
    static int sum(int... xs) {
        int s = 0;
        for (int x : xs) s += x;
        return s;
    }

    public static void main(String[] args) {
        if (sum(1, 2, 3, 4) != 10) throw new AssertionError("varargs sum");
        if (sum() != 0) throw new AssertionError("varargs empty");
        System.out.println("PASS");
    }
}
