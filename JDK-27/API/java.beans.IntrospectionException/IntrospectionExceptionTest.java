/*
 * @test
 * @summary java.beans.IntrospectionException conformance: message and hierarchy.
 * @run main IntrospectionExceptionTest
 */

import java.beans.*;

public class IntrospectionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        IntrospectionException e = new IntrospectionException("boom");
        check("boom".equals(e.getMessage()), "message");
        check(e instanceof Exception, "extends Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
