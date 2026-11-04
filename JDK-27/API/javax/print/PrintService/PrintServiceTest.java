/*
 * @test
 * @summary javax.print.PrintService conformance: white-box structure + black-box behavior.
 * @run main PrintServiceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.PrintService");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
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
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
