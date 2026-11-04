/*
 * @test
 * @summary javax.management.RuntimeErrorException conformance: full contract
 *          coverage. target Error wrapping (getTargetError/getCause), both
 *          constructors, and JMRuntimeException hierarchy.
 * @run main RuntimeErrorExceptionTest
 */

import javax.management.RuntimeErrorException;

public class RuntimeErrorExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Error target = new OutOfMemoryError("oom");
        RuntimeErrorException x = new RuntimeErrorException(target);
        check(x.getTargetError() == target, "getTargetError");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMRuntimeException, "extends JMRuntimeException");
        check(x instanceof RuntimeException, "extends RuntimeException");

        RuntimeErrorException y = new RuntimeErrorException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetError() == target, "message ctor target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
