/*
 * @test
 * @summary javax.management.InstanceAlreadyExistsException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main InstanceAlreadyExistsExceptionTest
 */

import javax.management.InstanceAlreadyExistsException;

public class InstanceAlreadyExistsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        InstanceAlreadyExistsException x = new InstanceAlreadyExistsException("dup");
        check("dup".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        InstanceAlreadyExistsException noArg = new InstanceAlreadyExistsException();
        check(noArg.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
