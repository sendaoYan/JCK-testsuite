/*
 * @test
 * @summary JLS 4.12.4 / 15.27.2: a local variable that is effectively final
 *          (assigned exactly once, never reassigned) may be captured by a lambda
 *          expression.
 * @run main EffectivelyFinalLambda
 */

public class EffectivelyFinalLambda {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int x = 40;                 // effectively final: assigned once, never reassigned
        java.util.function.IntSupplier s = () -> x + 2;
        check(s.getAsInt() == 42, "lambda captures effectively final int");

        String msg = "hello";       // effectively final
        java.util.function.Supplier<String> t = () -> msg.toUpperCase();
        check("HELLO".equals(t.get()), "lambda captures effectively final String");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
