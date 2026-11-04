/*
 * @test
 * @summary JLS 18.5.3 (Java SE 8): a lambda's parameter types may be given
 *          explicitly or left to be inferred from the target type.
 * @run main LambdaExplicitTypeParams
 */

import java.util.function.IntBinaryOperator;

public class LambdaExplicitTypeParams {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        IntBinaryOperator inferred = (a, b) -> a + b;
        IntBinaryOperator explicit = (int a, int b) -> a + b;
        check(inferred.applyAsInt(3, 4) == 7, "inferred lambda parameter types");
        check(explicit.applyAsInt(3, 4) == 7, "explicit lambda parameter types");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
