/*
 * @test
 * @summary javax.print.DocPrintJob conformance: white-box structure + black-box behavior.
 * @run main DocPrintJobTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DocPrintJobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocPrintJob");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "getPrintService"), "declares getPrintService");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "addPrintJobListener"), "declares addPrintJobListener");
        check(hasMethod(c, "removePrintJobListener"), "declares removePrintJobListener");
        check(hasMethod(c, "addPrintJobAttributeListener"), "declares addPrintJobAttributeListener");
        check(hasMethod(c, "removePrintJobAttributeListener"), "declares removePrintJobAttributeListener");
        check(hasMethod(c, "print"), "declares print");
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
