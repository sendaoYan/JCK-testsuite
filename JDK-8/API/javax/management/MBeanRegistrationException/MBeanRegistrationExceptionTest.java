/*
 * @test
 * @summary javax.management.MBeanRegistrationException conformance: full
 *          contract coverage. target Exception wrapping (getTargetException/
 *          getCause), both constructors, and MBeanException hierarchy.
 * @run main MBeanRegistrationExceptionTest
 */

import javax.management.MBeanRegistrationException;

public class MBeanRegistrationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Exception target = new IllegalStateException("boom");
        MBeanRegistrationException x = new MBeanRegistrationException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.MBeanException, "extends MBeanException");
        check(x instanceof javax.management.JMException, "extends JMException");

        MBeanRegistrationException y = new MBeanRegistrationException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
