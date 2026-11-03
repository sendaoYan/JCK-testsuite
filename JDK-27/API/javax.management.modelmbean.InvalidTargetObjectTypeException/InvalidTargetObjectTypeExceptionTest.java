/*
 * @test
 * @summary javax.management.modelmbean.InvalidTargetObjectTypeException conformance: white-box structure + black-box behavior.
 * @run main InvalidTargetObjectTypeExceptionTest
 */

import javax.management.modelmbean.InvalidTargetObjectTypeException;

public class InvalidTargetObjectTypeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.InvalidTargetObjectTypeException");
        check(Exception.class.isAssignableFrom(c), "extends Exception");
        check(c.getSuperclass() == Exception.class, "direct superclass is Exception");
        check(c.getConstructors().length >= 3, "has all three constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InvalidTargetObjectTypeException e1 = new InvalidTargetObjectTypeException("boom");
        check("InvalidTargetObjectTypeException: boom".equals(e1.getMessage()), "String message");
        check(e1 instanceof Exception, "instance of Exception");

        InvalidTargetObjectTypeException e2 = new InvalidTargetObjectTypeException();
        check("InvalidTargetObjectTypeException: ".equals(e2.getMessage()), "no-arg message");

        Exception cause = new Exception("cause");
        InvalidTargetObjectTypeException e3 = new InvalidTargetObjectTypeException(cause, "msg2");
        check(e3.getMessage().startsWith("InvalidTargetObjectTypeException: msg2"), "target message prefix");
        check(e3.getMessage().contains("triggered by:"), "target message embeds cause");

        InvalidTargetObjectTypeException e4 = new InvalidTargetObjectTypeException(null, "msg3");
        check("InvalidTargetObjectTypeException: msg3".equals(e4.getMessage()), "null cause message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
