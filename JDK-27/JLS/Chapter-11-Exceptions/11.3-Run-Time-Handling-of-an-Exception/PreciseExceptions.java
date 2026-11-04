/*
 * @test
 * @summary JLS 11.3: exceptions are precise: effects of statements and expressions
 *          evaluated before the throw point appear to have taken place, and none
 *          after it appear to have been evaluated.
 * @run main PreciseExceptions
 */

public class PreciseExceptions {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int counter;

    public static void main(String[] args) {
        counter = 0;
        try {
            counter++;           // effect before the throw
            throw new RuntimeException();
            // unreachable; counter++ after throw never happens
        } catch (RuntimeException expected) {
            // expected
        }
        check(counter == 1, "only the effect before the throw took place");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
