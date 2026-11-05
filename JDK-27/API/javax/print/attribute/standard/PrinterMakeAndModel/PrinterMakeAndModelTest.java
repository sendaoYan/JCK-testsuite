/*
 * @test
 * @summary javax.print.attribute.standard.PrinterMakeAndModel conformance: white-box structure + black-box behavior.
 * @run main PrinterMakeAndModelTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterMakeAndModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterMakeAndModel");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(TextSyntax.class.isAssignableFrom(c), "extends TextSyntax");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PrinterMakeAndModel a = new PrinterMakeAndModel("report", Locale.ROOT);
        check("report".equals(a.getValue()), "getValue");
        check(Locale.ROOT.equals(a.getLocale()), "getLocale");
        check("printer-make-and-model".equals(a.getName()), "getName");
        check(a.getCategory() == PrinterMakeAndModel.class, "getCategory");
        check("report".equals(a.toString()), "toString");
        check(a.equals(new PrinterMakeAndModel("report", Locale.ROOT)), "equals same");
        check(!a.equals(new PrinterMakeAndModel("report2", Locale.ROOT)), "different value unequal");
        check(!a.equals(new PrinterMakeAndModel("report", Locale.US)), "different locale unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(a.hashCode() == new PrinterMakeAndModel("report", Locale.ROOT).hashCode(), "hashCode consistent");
        boolean threw = false; try { new PrinterMakeAndModel(null, Locale.ROOT); } catch (NullPointerException e) { threw = true; }
        check(threw, "null value throws NPE");
        PrinterMakeAndModel def = new PrinterMakeAndModel("x", null);
        check(Locale.getDefault().equals(def.getLocale()), "null locale -> default");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: null NPE / locale defaulting / equals / serialization ----
    static void testContractDeep() throws Exception {
        PrinterMakeAndModel v = new PrinterMakeAndModel("contract", java.util.Locale.FRENCH);
        check("contract".equals(v.getValue()), "getValue");
        check(java.util.Locale.FRENCH.equals(v.getLocale()), "getLocale");
        check("contract".equals(v.toString()), "toString == value");
        check("printer-make-and-model".equals(v.getName()), "getName");
        check(PrinterMakeAndModel.class == v.getCategory(), "getCategory");
        expectNPE("PrinterMakeAndModel(null, locale)", new Runnable() { public void run() { new PrinterMakeAndModel(null, java.util.Locale.FRENCH); } }, " value is null");
        PrinterMakeAndModel dl = new PrinterMakeAndModel("x", null);
        check(java.util.Locale.getDefault().equals(dl.getLocale()), "null locale -> default locale");
        check(v.equals(new PrinterMakeAndModel("contract", java.util.Locale.FRENCH)), "equals same value+locale");
        check(!v.equals(new PrinterMakeAndModel("contract", java.util.Locale.ENGLISH)), "different locale unequal");
        check(!v.equals(new PrinterMakeAndModel("other", java.util.Locale.FRENCH)), "different value unequal");
        check(!v.equals(null) && !v.equals("contract"), "null/foreign unequal");
        check(v.hashCode() == new PrinterMakeAndModel("contract", java.util.Locale.FRENCH).hashCode(), "hashCode consistent");
        PrinterMakeAndModel rt = (PrinterMakeAndModel) serRoundTrip(v);
        check(rt.equals(v) && "contract".equals(rt.getValue()) && java.util.Locale.FRENCH.equals(rt.getLocale()), "serialization round-trip");
        PrinterMakeAndModel en = new PrinterMakeAndModel("mixed", java.util.Locale.ENGLISH);
        check(!v.equals(en) && !en.equals(v), "locale asymmetry none");
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
