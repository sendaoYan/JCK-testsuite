/*
 * @test
 * @summary java.lang.TypeNotPresentException API conformance: typeName/cause.
 * @run main TypeNotPresentExceptionTest
 */

public class TypeNotPresentExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTypeNotPresentException() {
        RuntimeException cause = new RuntimeException();
        TypeNotPresentException e = new TypeNotPresentException("java.lang.Nope", cause);
        check(e instanceof RuntimeException, "extends RuntimeException");
        check("java.lang.Nope".equals(e.typeName()), "typeName");
        check(e.getCause() == cause, "getCause");
    }

    public static void main(String[] args) {
        testTypeNotPresentException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
