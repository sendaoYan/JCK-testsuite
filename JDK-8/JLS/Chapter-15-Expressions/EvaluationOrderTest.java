/*
 * @test
 * @summary JLS 15.7 (Java SE 8): operands of an operator and arguments of a method
 *          invocation are evaluated left to right.
 * @run main EvaluationOrderTest
 */

public class EvaluationOrderTest {
    static StringBuilder log = new StringBuilder();
    static int f(String tag, int v) { log.append(tag); return v; }

    public static void main(String[] args) {
        int r = f("a", 1) + f("b", 2) * f("c", 3);
        if (!log.toString().equals("abc")) throw new AssertionError("left-to-right: " + log);
        if (r != 7) throw new AssertionError("1 + 2*3 = 7");
        System.out.println("PASS");
    }
}
