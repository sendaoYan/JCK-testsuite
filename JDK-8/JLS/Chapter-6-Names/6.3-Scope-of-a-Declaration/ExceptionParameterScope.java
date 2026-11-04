/*
 * @test
 * @summary JLS 6.3 (Java SE 8): the scope of an exception parameter is the block
 *          of its catch clause; separate catch clauses may reuse the same name.
 * @run main ExceptionParameterScope
 */

public class ExceptionParameterScope {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        try {
            throw new IllegalStateException("a");
        } catch (IllegalStateException e) {
            check(e.getMessage().equals("a"), "catch parameter in scope in its clause");
        }
        try {
            throw new IllegalArgumentException("b");
        } catch (IllegalArgumentException e) {   // reused name in a separate clause
            check(e.getMessage().equals("b"), "second catch clause reuses the name");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
