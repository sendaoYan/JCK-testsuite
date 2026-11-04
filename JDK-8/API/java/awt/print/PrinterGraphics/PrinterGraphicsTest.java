/*
 * @test
 * @summary java.awt.print.PrinterGraphics conformance: interface reflection.
 * @run main PrinterGraphicsTest
 */

public class PrinterGraphicsTest {
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
        Class<?> c = Class.forName("java.awt.print.PrinterGraphics");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getPrinterJob"), "method getPrinterJob");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
