/*
 * @test
 * @summary JLS 8.4.8.3: a private method cannot be overridden, so a subclass may
 *          declare a method with the same signature and an unrelated return
 *          type.
 * @run main PrivateMethodBehavesAsFinal
 */
class Base {
    private int f() { return 1; }
}
public class PrivateMethodBehavesAsFinal extends Base {
    String f() { return "not-overriding"; }
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        check(new PrivateMethodBehavesAsFinal().f().equals("not-overriding"), "subclass method unrelated to private superclass method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
