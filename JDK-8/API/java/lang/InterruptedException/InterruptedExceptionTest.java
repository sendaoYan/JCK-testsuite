/*
 * @test
 * @summary java.lang.InterruptedException API conformance: checked-exception hierarchy
 *          (InterruptedException -> Exception), all four constructors, and message/cause
 *          propagation.
 * @run main InterruptedExceptionTest
 */

public class InterruptedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        InterruptedException e = new InterruptedException();
        check(e instanceof Exception, "InterruptedException is an Exception");
        check(!((Object) e instanceof RuntimeException),
              "InterruptedException is checked (not RuntimeException)");
    }

    static void testConstructors() {
        InterruptedException e1 = new InterruptedException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        InterruptedException e2 = new InterruptedException("msg");
        check("msg".equals(e2.getMessage()), "(String) ctor");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
