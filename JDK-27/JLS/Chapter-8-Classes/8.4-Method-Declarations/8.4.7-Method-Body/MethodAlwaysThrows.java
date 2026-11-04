/*
 * @test
 * @summary JLS 8.4.7: a method with a return type may contain no return
 *          statement if its body can never complete normally (always throws).
 * @run main MethodAlwaysThrows
 */
public class MethodAlwaysThrows {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    int pitch() { throw new RuntimeException("90 mph"); }
    public static void main(String[] args) {
        boolean thrown = false;
        try { new MethodAlwaysThrows().pitch(); } catch (RuntimeException e) { thrown = true; }
        check(thrown, "method with return type but no return statement may always throw");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
