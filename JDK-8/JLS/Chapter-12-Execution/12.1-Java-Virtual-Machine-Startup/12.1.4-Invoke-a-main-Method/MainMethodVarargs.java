/*
 * @test
 * @summary JLS 12.1.4 (Java SE 8): the main method must be public, static, and
 *          void with a single formal parameter of type String[]; String... denotes
 *          the same type.
 * @run main MainMethodVarargs alpha beta
 */

public class MainMethodVarargs {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String... args) {
        check(args.length == 2, "argument count: " + args.length);
        check("alpha".equals(args[0]) && "beta".equals(args[1]), "argument values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
