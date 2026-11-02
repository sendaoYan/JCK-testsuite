/*
 * @test
 * @summary javax.print.CancelablePrintJob conformance: interface.
 * @run main CancelablePrintJobTest
 */

import java.lang.reflect.*;


public class CancelablePrintJobTest {
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
        Class<?> c = Class.forName("javax.print.CancelablePrintJob");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasIf(c, "javax.print.DocPrintJob"), "extends javax.print.DocPrintJob");
        check(hasMethod(c, "cancel"), "declares cancel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
