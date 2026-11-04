/*
 * @test
 * @summary java.awt.AWTError conformance: class reflection.
 * @run main AWTErrorTest
 */

public class AWTErrorTest {
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
        Class<?> c = Class.forName("java.awt.AWTError");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Error"), "superclass java.lang.Error");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        java.awt.AWTError err = new java.awt.AWTError("boom");
        check(err.getMessage().equals("boom"), "AWTError message preserved");
        check(err instanceof java.lang.Error, "AWTError extends Error");
        check(err instanceof java.io.Serializable, "Error is Serializable");
        check(err.getCause() == null, "no cause");
        check(err.getStackTrace() != null, "stack trace accessible");
        java.awt.AWTError empty = new java.awt.AWTError("");
        check(empty.getMessage().equals(""), "empty message preserved");
        boolean iae = false;
        try { new java.awt.AWTError(null); } catch (java.lang.NullPointerException e) { iae = true; }
        check(!iae, "null message tolerated (Throwable)");
        check(err.toString().startsWith("java.awt.AWTError"), "toString class-prefixed: " + err);
    }

}
