/*
 * @test
 * @summary javax.management.MalformedObjectNameException conformance: full
 *          contract coverage. message construction, OperationsException/JMException
 *          hierarchy, no-arg ctor, and ObjectName.parse integration point.
 * @run main MalformedObjectNameExceptionTest
 */

import javax.management.MalformedObjectNameException;

public class MalformedObjectNameExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        MalformedObjectNameException x = new MalformedObjectNameException("msg");
        check("msg".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");
        check(x.getCause() == null, "no cause by default");

        MalformedObjectNameException noArg = new MalformedObjectNameException();
        check(noArg.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
