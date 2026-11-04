/*
 * @test
 * @summary javax.management.RuntimeOperationsException conformance: full
 *          contract coverage. target RuntimeException wrapping
 *          (getTargetException/getCause), both constructors, and JMRuntimeException
 *          hierarchy.
 * @run main RuntimeOperationsExceptionTest
 */

import javax.management.RuntimeOperationsException;

public class RuntimeOperationsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        RuntimeException target = new IndexOutOfBoundsException("oops");
        RuntimeOperationsException x = new RuntimeOperationsException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMRuntimeException, "extends JMRuntimeException");
        check(x instanceof RuntimeException, "extends RuntimeException");

        RuntimeOperationsException y = new RuntimeOperationsException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
