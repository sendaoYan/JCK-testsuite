/*
 * @test
 * @summary JLS 8.4.1 (Java SE 8): a receiver parameter may appear in the declarator of an
 *          instance method; its type is the enclosing class and its name is this.
 * @run main ReceiverParameterInstanceMethod
 */
public class ReceiverParameterInstanceMethod {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    int m(ReceiverParameterInstanceMethod this) { return 1; }
    public static void main(String[] args) {
        check(new ReceiverParameterInstanceMethod().m() == 1, "receiver parameter in instance method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
