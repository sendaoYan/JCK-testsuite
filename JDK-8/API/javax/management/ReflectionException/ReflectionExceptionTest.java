/*
 * @test
 * @summary javax.management.ReflectionException conformance: full contract
 *          coverage. target Exception wrapping (getTargetException/getCause),
 *          both constructors, and JMException hierarchy.
 * @run main ReflectionExceptionTest
 */

import javax.management.ReflectionException;

public class ReflectionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Exception target = new NoSuchMethodException("nope");
        ReflectionException x = new ReflectionException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMException, "extends JMException");

        ReflectionException y = new ReflectionException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
