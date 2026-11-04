/*
 * @test
 * @summary JLS 8.3.3 (Java SE 8): a reference in a different class (an anonymous class body)
 *          is not subject to the forward-reference restriction, and reads the
 *          field's default value before its initializer runs.
 * @run main ForwardReferenceInAnonymousClass
 */
public class ForwardReferenceInAnonymousClass {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static int i = new Object() { int read() { return j; } }.read();
    static int j = 7;
    public static void main(String[] args) {
        check(i == 0, "anonymous class reading forward field observes default value");
        check(j == 7, "field initialized after");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
