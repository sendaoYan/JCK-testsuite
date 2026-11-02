/*
 * @test
 * @summary javax.print.attribute.UnmodifiableSetException conformance: exception class.
 * @run main UnmodifiableSetExceptionTest
 */

import java.lang.reflect.*;


public class UnmodifiableSetExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        javax.print.attribute.UnmodifiableSetException e1 = new javax.print.attribute.UnmodifiableSetException();
        check(e1.getMessage() == null, "default message");
        check(e1 instanceof RuntimeException, "extends RuntimeException");
        javax.print.attribute.UnmodifiableSetException e2 = new javax.print.attribute.UnmodifiableSetException("m");
        check("m".equals(e2.getMessage()), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
