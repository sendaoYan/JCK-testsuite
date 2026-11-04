/*
 * @test
 * @summary javax.print.attribute.standard.PrinterStateReasons conformance: white-box structure + black-box behavior.
 * @run main PrinterStateReasonsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterStateReasonsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterStateReasons");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(java.util.HashMap.class.isAssignableFrom(c), "extends HashMap");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PrinterStateReasons s = new PrinterStateReasons();
        s.put(PrinterStateReason.TONER_LOW, Severity.WARNING);
        check(Severity.WARNING == s.get(PrinterStateReason.TONER_LOW), "put/get");
        java.util.Set<PrinterStateReason> warned = s.printerStateReasonSet(Severity.WARNING);
        check(warned.size() == 1 && warned.contains(PrinterStateReason.TONER_LOW), "printerStateReasonSet");
        check(s.printerStateReasonSet(Severity.ERROR).isEmpty(), "empty severity set");
        check("printer-state-reasons".equals(s.getName()), "getName");
        check(s.getCategory() == PrinterStateReasons.class, "getCategory");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
