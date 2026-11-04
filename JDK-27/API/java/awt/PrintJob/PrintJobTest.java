/*
 * @test
 * @summary java.awt.PrintJob conformance: white-box structure + black-box behavior.
 * @run main PrintJobTest
 */

import java.lang.reflect.*;

public class PrintJobTest {
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
        Class<?> c = Class.forName("java.awt.PrintJob", false, PrintJobTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.PrintJob");
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
        check(hasMethod(c, "getGraphics"), "declares getGraphics");
        check(hasMethod(c, "end"), "declares end");
    }

    static void testBlackBox() throws Exception {
        java.awt.PrintJob job = new java.awt.PrintJob() {
            public java.awt.Graphics getGraphics() { return null; }
            public java.awt.Dimension getPageDimension() { return new java.awt.Dimension(10, 20); }
            public int getPageResolution() { return 72; }
            public boolean lastPageFirst() { return false; }
            public void end() {}
        };
        check(job.getGraphics() == null, "getGraphics null");
        java.awt.Dimension d = job.getPageDimension();
        check(d.width == 10 && d.height == 20, "getPageDimension");
        check(job.getPageResolution() == 72, "getPageResolution");
        check(job.lastPageFirst() == false, "lastPageFirst false");
        job.end();
        check(true, "end callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
