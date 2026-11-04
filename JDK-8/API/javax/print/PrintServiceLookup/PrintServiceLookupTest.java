/*
 * @test
 * @summary javax.print.PrintServiceLookup conformance: service lookup.
 * @run main PrintServiceLookupTest
 */

import java.lang.reflect.*;


public class PrintServiceLookupTest {
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
        javax.print.PrintService[] ps = javax.print.PrintServiceLookup.lookupPrintServices(null, null);
        check(ps != null, "lookupPrintServices non-null");
        javax.print.MultiDocPrintService[] md = javax.print.PrintServiceLookup.lookupMultiDocPrintServices(null, null);
        check(md != null, "lookupMultiDocPrintServices non-null");
        javax.print.PrintServiceLookup.lookupDefaultPrintService();
        javax.print.PrintServiceLookup.registerService(null);
        Class<?> c = Class.forName("javax.print.PrintServiceLookup");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(hasMethod(c, "getPrintServices"), "declares getPrintServices");
        check(hasMethod(c, "getDefaultPrintService"), "declares getDefaultPrintService");
        check(hasMethod(c, "getMultiDocPrintServices"), "declares getMultiDocPrintServices");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
