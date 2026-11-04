/*
 * @test
 * @summary JLS 8.4.6 (Java SE 8): it is permitted but not required to mention unchecked
 *          exception classes in a throws clause.
 * @run main ThrowsUncheckedExceptionPermitted
 */
public class ThrowsUncheckedExceptionPermitted {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    void m() throws RuntimeException { }
    public static void main(String[] args) {
        new ThrowsUncheckedExceptionPermitted().m();
        check(true, "unchecked exception permitted in throws clause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
