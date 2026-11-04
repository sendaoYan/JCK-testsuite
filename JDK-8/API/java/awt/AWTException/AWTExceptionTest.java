/*
 * @test
 * @summary java.awt.AWTException conformance: class reflection.
 * @run main AWTExceptionTest
 */

public class AWTExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.AWTException");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        java.awt.AWTException ex = new java.awt.AWTException("why");
        check(ex.getMessage().equals("why"), "AWTException message preserved");
        check(ex instanceof java.lang.Exception, "AWTException checked Exception");
        check(ex instanceof java.io.Serializable, "Exception is Serializable");
        check(ex.getCause() == null, "no cause");
        java.awt.AWTException empty = new java.awt.AWTException("");
        check(empty.getMessage().equals(""), "empty message");
        check(ex.toString().startsWith("java.awt.AWTException"), "toString class-prefixed: " + ex);
        // declaring code must handle it: verify via a throwing method
        try {
            throwIfTrue(true);
            check(false, "unreachable");
        } catch (java.awt.AWTException e) {
            check(e.getMessage().equals("expected"), "caught declared AWTException");
        }
    }
    static void throwIfTrue(boolean b) throws java.awt.AWTException {
        if (b) throw new java.awt.AWTException("expected");
    }

}
