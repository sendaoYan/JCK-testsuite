/*
 * @test
 * @summary java.util.function.ToDoubleBiFunction API conformance: applyAsDouble.
 * @run main ToDoubleBiFunctionTest
 */

import java.util.function.ToDoubleBiFunction;

public class ToDoubleBiFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testToDoubleBiFunction() {
        ToDoubleBiFunction<String, Integer> f = (s, i) -> s.length() + i;
        check(f.applyAsDouble("ab", 3) == 5.0, "applyAsDouble");
    }

    public static void main(String[] args) {
        testToDoubleBiFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
