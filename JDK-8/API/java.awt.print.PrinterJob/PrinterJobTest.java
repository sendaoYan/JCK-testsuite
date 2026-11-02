/*
 * @test
 * @summary java.awt.print.PrinterJob conformance: abstract class reflection.
 * @run main PrinterJobTest
 */

public class PrinterJobTest {
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
        Class<?> c = Class.forName("java.awt.print.PrinterJob");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getPrinterJob"), "method getPrinterJob");
        check(hasMethod(c, "lookupPrintServices"), "method lookupPrintServices");
        check(hasMethod(c, "lookupStreamPrintServices"), "method lookupStreamPrintServices");
        check(hasMethod(c, "getPrintService"), "method getPrintService");
        check(hasMethod(c, "setPrintService"), "method setPrintService");
        check(hasMethod(c, "setPrintable"), "method setPrintable");
        check(hasMethod(c, "setPageable"), "method setPageable");
        check(hasMethod(c, "printDialog"), "method printDialog");
        check(hasMethod(c, "pageDialog"), "method pageDialog");
        check(hasMethod(c, "defaultPage"), "method defaultPage");
        check(hasMethod(c, "getPageFormat"), "method getPageFormat");
        check(hasMethod(c, "validatePage"), "method validatePage");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "setCopies"), "method setCopies");
        check(hasMethod(c, "getCopies"), "method getCopies");
        check(hasMethod(c, "getUserName"), "method getUserName");
        check(hasMethod(c, "setJobName"), "method setJobName");
        check(hasMethod(c, "getJobName"), "method getJobName");
        check(hasMethod(c, "cancel"), "method cancel");
        check(hasMethod(c, "isCancelled"), "method isCancelled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
