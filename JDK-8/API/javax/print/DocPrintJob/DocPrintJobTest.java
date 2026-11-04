/*
 * @test
 * @summary javax.print.DocPrintJob conformance: interface.
 * @run main DocPrintJobTest
 */

import java.lang.reflect.*;


public class DocPrintJobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocPrintJob");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getPrintService"), "declares getPrintService");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "addPrintJobListener"), "declares addPrintJobListener");
        check(hasMethod(c, "removePrintJobListener"), "declares removePrintJobListener");
        check(hasMethod(c, "addPrintJobAttributeListener"), "declares addPrintJobAttributeListener");
        check(hasMethod(c, "removePrintJobAttributeListener"), "declares removePrintJobAttributeListener");
        check(hasMethod(c, "print"), "declares print");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
