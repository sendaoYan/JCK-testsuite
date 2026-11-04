/*
 * @test
 * @summary java.awt.print.PrinterIOException conformance: class reflection.
 * @run main PrinterIOExceptionTest
 */

public class PrinterIOExceptionTest {
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
        Class<?> c = Class.forName("java.awt.print.PrinterIOException");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.print.PrinterException"), "superclass java.awt.print.PrinterException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getIOException"), "method getIOException");
        check(hasMethod(c, "getCause"), "method getCause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
