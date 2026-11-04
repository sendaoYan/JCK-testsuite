/*
 * @test
 * @summary java.awt.print.PrinterAbortException conformance: white-box structure + black-box behavior.
 * @run main PrinterAbortExceptionTest
 */

import java.lang.reflect.*;

public class PrinterAbortExceptionTest {
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
        Class<?> c = Class.forName("java.awt.print.PrinterAbortException", false, PrinterAbortExceptionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.print.PrinterAbortException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.awt.print.PrinterException"), "extends PrinterException");
    }

    static void testBlackBox() throws Exception {
        java.awt.print.PrinterAbortException e1 = new java.awt.print.PrinterAbortException();
        java.awt.print.PrinterAbortException e2 = new java.awt.print.PrinterAbortException("aborted");
        check("aborted".equals(e2.getMessage()), "message ctor");
        check(java.awt.print.PrinterException.class.isInstance(e2), "instanceof PrinterException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
