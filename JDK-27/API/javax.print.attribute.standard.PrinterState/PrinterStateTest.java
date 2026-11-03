/*
 * @test
 * @summary javax.print.attribute.standard.PrinterState conformance: white-box structure + black-box behavior.
 * @run main PrinterStateTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterState");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(PrinterState.UNKNOWN.getCategory() == PrinterState.class, "getCategory");
        check("printer-state".equals(PrinterState.UNKNOWN.getName()), "getName");
        check(PrinterState.UNKNOWN.getValue() == 0, "UNKNOWN value == 0");
        check("unknown".equals(PrinterState.UNKNOWN.toString()), "UNKNOWN toString");
        check(PrinterState.IDLE.getValue() == 3, "IDLE value == 3");
        check("idle".equals(PrinterState.IDLE.toString()), "IDLE toString");
        check(PrinterState.PROCESSING.getValue() == 4, "PROCESSING value == 4");
        check("processing".equals(PrinterState.PROCESSING.toString()), "PROCESSING toString");
        check(PrinterState.STOPPED.getValue() == 5, "STOPPED value == 5");
        check("stopped".equals(PrinterState.STOPPED.toString()), "STOPPED toString");
        check(PrinterState.UNKNOWN.clone() == PrinterState.UNKNOWN, "clone returns singleton");
        check(PrinterState.UNKNOWN.hashCode() == 0, "hashCode == value");
        check(PrinterState.UNKNOWN.equals(PrinterState.UNKNOWN), "equals self");
        check(!PrinterState.UNKNOWN.equals(PrinterState.IDLE), "distinct constants unequal");
        check(!PrinterState.UNKNOWN.equals(null) && !PrinterState.UNKNOWN.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
