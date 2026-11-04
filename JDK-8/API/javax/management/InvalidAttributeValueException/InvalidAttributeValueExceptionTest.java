/*
 * @test
 * @summary javax.management.InvalidAttributeValueException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main InvalidAttributeValueExceptionTest
 */

import javax.management.InvalidAttributeValueException;

public class InvalidAttributeValueExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        InvalidAttributeValueException x = new InvalidAttributeValueException("bad");
        check("bad".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        InvalidAttributeValueException noArg = new InvalidAttributeValueException();
        check(noArg.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
