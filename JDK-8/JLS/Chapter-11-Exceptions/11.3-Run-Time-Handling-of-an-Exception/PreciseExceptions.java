/*
 * @test
 * @summary JLS 11.3 (Java SE 8): exceptions are precise; effects before the throw
 *          appear to have taken place and effects after it do not.
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
            counter++;
            throw new RuntimeException();
        } catch (RuntimeException expected) {
            // expected
        }
        check(counter == 1, "only the effect before the throw took place");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
