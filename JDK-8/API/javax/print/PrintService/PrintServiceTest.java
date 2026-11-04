/*
 * @test
 * @summary javax.print.PrintService conformance: interface.
 * @run main PrintServiceTest
 */

import java.lang.reflect.*;


public class PrintServiceTest {
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
        Class<?> c = Class.forName("javax.print.PrintService");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "createPrintJob"), "declares createPrintJob");
        check(hasMethod(c, "addPrintServiceAttributeListener"), "declares addPrintServiceAttributeListener");
        check(hasMethod(c, "removePrintServiceAttributeListener"), "declares removePrintServiceAttributeListener");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "getSupportedDocFlavors"), "declares getSupportedDocFlavors");
        check(hasMethod(c, "isDocFlavorSupported"), "declares isDocFlavorSupported");
        check(hasMethod(c, "getSupportedAttributeCategories"), "declares getSupportedAttributeCategories");
        check(hasMethod(c, "isAttributeCategorySupported"), "declares isAttributeCategorySupported");
        check(hasMethod(c, "getDefaultAttributeValue"), "declares getDefaultAttributeValue");
        check(hasMethod(c, "getSupportedAttributeValues"), "declares getSupportedAttributeValues");
        check(hasMethod(c, "isAttributeValueSupported"), "declares isAttributeValueSupported");
        check(hasMethod(c, "getUnsupportedAttributes"), "declares getUnsupportedAttributes");
        check(hasMethod(c, "getServiceUIFactory"), "declares getServiceUIFactory");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
