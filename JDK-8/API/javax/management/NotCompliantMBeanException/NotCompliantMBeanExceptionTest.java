/*
 * @test
 * @summary javax.management.NotCompliantMBeanException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main NotCompliantMBeanExceptionTest
 */

import javax.management.NotCompliantMBeanException;

public class NotCompliantMBeanExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        NotCompliantMBeanException x = new NotCompliantMBeanException("bad");
        check("bad".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        NotCompliantMBeanException noArg = new NotCompliantMBeanException();
        check(noArg.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
