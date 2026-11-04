/*
 * @test
 * @summary javax.management.MBeanException conformance: full contract coverage.
 *          target-exception wrapping (getTargetException/getCause), both
 *          constructors (message defaulting), and JMException hierarchy.
 * @run main MBeanExceptionTest
 */

import javax.management.MBeanException;

public class MBeanExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Exception target = new IllegalArgumentException("boom");
        MBeanException x = new MBeanException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        MBeanException y = new MBeanException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");

        MBeanException noArg = new MBeanException(new Exception());
        check(noArg.getMessage() == null && noArg.getCause() != null, "no-arg + target only");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
