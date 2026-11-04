/*
 * @test
 * @summary JLS 18.5.1: in a loose invocation, primitive arguments are boxed or
 *          widened so a common supertype can be inferred for a type parameter.
 * @run main BoxingLooseInvocation
 */

public class BoxingLooseInvocation {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static <T> T pick(T a, T b) { return a; }

    public static void main(String[] args) {
        Number n = pick(1, 2.0);   // int -> Integer, double -> Double; T inferred as Number
        check(n.doubleValue() == 1.0, "loose invocation infers T=Number for int+double");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
