/*
 * @test
 * @summary javax.management.InstanceNotFoundException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main InstanceNotFoundExceptionTest
 */

import javax.management.InstanceNotFoundException;

public class InstanceNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        InstanceNotFoundException x = new InstanceNotFoundException("name");
        check("name".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        InstanceNotFoundException noArg = new InstanceNotFoundException();
        check(noArg.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
