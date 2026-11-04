/*
 * @test
 * @summary javax.management.AttributeNotFoundException conformance: full
 *          contract coverage. message construction, no-arg ctor, and
 *          OperationsException/JMException hierarchy.
 * @run main AttributeNotFoundExceptionTest
 */

import javax.management.AttributeNotFoundException;

public class AttributeNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        AttributeNotFoundException x = new AttributeNotFoundException("attr");
        check("attr".equals(x.getMessage()), "message");
        check(x instanceof javax.management.OperationsException, "extends OperationsException");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        AttributeNotFoundException noArg = new AttributeNotFoundException();
        check(noArg.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
