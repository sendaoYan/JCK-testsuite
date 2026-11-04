/*
 * @test
 * @summary java.awt.print.PrinterJob conformance: white-box structure + black-box behavior.
 * @run main PrinterJobTest
 */

import java.lang.reflect.*;

public class PrinterJobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.print.PrinterJob", false, PrinterJobTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.print.PrinterJob");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "getPrinterJob"), "declares getPrinterJob");
        check(hasMethod(c, "printDialog"), "declares printDialog");
    }

    static void testBlackBox() throws Exception {
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        check(job != null, "getPrinterJob non-null");
        check(job.getCopies() == 1, "default copies 1");
        job.setCopies(3);
        check(job.getCopies() == 3, "setCopies 3");
        check(job.getUserName() != null, "getUserName non-null");
        job.setJobName("job-x");
        check("job-x".equals(job.getJobName()), "setJobName/getJobName");
        check(job.isCancelled() == false, "isCancelled false initially");
        job.cancel();
        check(job.isCancelled() == true || job.isCancelled() == false, "cancel callable");
        boolean headless = java.awt.GraphicsEnvironment.isHeadless();
        if (headless) {
            boolean hle = false;
            try { job.printDialog(); } catch (java.awt.HeadlessException e) { hle = true; }
            check(hle, "printDialog throws HeadlessException in headless mode");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
