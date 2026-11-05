/*
 * @test
 * @summary javax.print.attribute.PrintServiceAttribute conformance: white-box structure + black-box behavior.
 * @run main PrintServiceAttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintServiceAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.PrintServiceAttribute");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(Attribute.class.isAssignableFrom(c), "extends Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PrintServiceAttribute a = new PagesPerMinute(5);
        check("pages-per-minute".equals(a.getName()), "PrintServiceAttribute marker");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: marker interface membership matrix ----
    static void testContractDeep() throws Exception {
        // DocAttribute members
        check(javax.print.attribute.DocAttribute.class.isInstance(javax.print.attribute.standard.Chromaticity.MONOCHROME), "Chromaticity is DocAttribute");
        check(!javax.print.attribute.DocAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is NOT DocAttribute");
        check(javax.print.attribute.DocAttribute.class.isInstance(new javax.print.attribute.standard.CopiesSupported(1, 5)) == false, "CopiesSupported is NOT DocAttribute");
        check(javax.print.attribute.DocAttribute.class.isInstance(new javax.print.attribute.standard.MediaPrintableArea(1, 1, 10, 10, 25400)), "MediaPrintableArea is DocAttribute");
        // PrintRequestAttribute members
        check(javax.print.attribute.PrintRequestAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is PrintRequestAttribute");
        check(javax.print.attribute.PrintRequestAttribute.class.isInstance(new javax.print.attribute.standard.JobName("j", null)), "JobName is PrintRequestAttribute");
        check(!javax.print.attribute.PrintRequestAttribute.class.isInstance(javax.print.attribute.standard.PrinterState.IDLE), "PrinterState is NOT PrintRequestAttribute");
        // PrintJobAttribute members
        check(javax.print.attribute.PrintJobAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is PrintJobAttribute");
        check(javax.print.attribute.PrintJobAttribute.class.isInstance(javax.print.attribute.standard.JobState.PENDING), "JobState is PrintJobAttribute");
        check(!javax.print.attribute.PrintJobAttribute.class.isInstance(new javax.print.attribute.standard.PrinterName("p", null)), "PrinterName is NOT PrintJobAttribute");
        // PrintServiceAttribute members
        check(javax.print.attribute.PrintServiceAttribute.class.isInstance(new javax.print.attribute.standard.PrinterName("p", null)), "PrinterName is PrintServiceAttribute");
        check(javax.print.attribute.PrintServiceAttribute.class.isInstance(javax.print.attribute.standard.PrinterState.IDLE), "PrinterState is PrintServiceAttribute");
        check(!javax.print.attribute.PrintServiceAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is NOT PrintServiceAttribute");
        // all four extend Attribute
        Class<?>[] ifaces = { javax.print.attribute.DocAttribute.class, javax.print.attribute.PrintRequestAttribute.class,
            javax.print.attribute.PrintJobAttribute.class, javax.print.attribute.PrintServiceAttribute.class };
        for (Class<?> i : ifaces)
            check(javax.print.attribute.Attribute.class.isAssignableFrom(i), i.getSimpleName() + " extends Attribute");
        // typed sets accept only matching attributes
        javax.print.attribute.HashDocAttributeSet docSet = new javax.print.attribute.HashDocAttributeSet();
        docSet.add(javax.print.attribute.standard.Chromaticity.MONOCHROME);
        try { docSet.add((javax.print.attribute.Attribute) new javax.print.attribute.standard.PrinterName("p", null)); throw new AssertionError("expected CCE"); }
        catch (ClassCastException e) { passed++; }
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
