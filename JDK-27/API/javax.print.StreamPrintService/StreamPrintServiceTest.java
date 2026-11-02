/*
 * @test
 * @summary javax.print.StreamPrintService conformance: abstract stream print service.
 * @run main StreamPrintServiceTest
 */

import java.lang.reflect.*;


public class StreamPrintServiceTest {
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
        Class<?> c = Class.forName("javax.print.StreamPrintService");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "javax.print.PrintService"), "implements javax.print.PrintService");
        check(hasMethod(c, "getOutputStream"), "declares getOutputStream");
        check(hasMethod(c, "getOutputFormat"), "declares getOutputFormat");
        check(hasMethod(c, "dispose"), "declares dispose");
        check(hasMethod(c, "isDisposed"), "declares isDisposed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
