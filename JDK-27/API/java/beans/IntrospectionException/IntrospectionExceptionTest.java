/*
 * @test
 * @summary java.beans.IntrospectionException conformance: white-box structure + black-box behavior.
 * @run main IntrospectionExceptionTest
 */

import java.beans.IntrospectionException;

public class IntrospectionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.IntrospectionException");
        check(Exception.class.isAssignableFrom(c), "extends Exception");
    }

    static void testBlackBox() throws Exception {
        IntrospectionException e = new IntrospectionException("boom");
        check("boom".equals(e.getMessage()), "message");
        IntrospectionException e2 = new IntrospectionException(null);
        check(e2.getMessage() == null, "null message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
