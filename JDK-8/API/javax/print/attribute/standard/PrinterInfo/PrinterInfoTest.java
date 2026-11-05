/*
 * @test
 * @summary javax.print.attribute.standard.PrinterInfo conformance: text attribute.
 * @run main PrinterInfoTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterInfo v = new PrinterInfo("sample-text", java.util.Locale.US);
        check("sample-text".equals(v.getValue()), "getValue");
        check(java.util.Locale.US.equals(v.getLocale()), "getLocale");
        check(v.getCategory() == PrinterInfo.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterInfo("sample-text", java.util.Locale.US)), "equals");
        check(v.hashCode() == new PrinterInfo("sample-text", java.util.Locale.US).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: null NPE / locale defaulting / equals / serialization ----
    static void testContractDeep() throws Exception {
        PrinterInfo v = new PrinterInfo("contract", java.util.Locale.FRENCH);
        check("contract".equals(v.getValue()), "getValue");
        check(java.util.Locale.FRENCH.equals(v.getLocale()), "getLocale");
        check("contract".equals(v.toString()), "toString == value");
        check("printer-info".equals(v.getName()), "getName");
        check(PrinterInfo.class == v.getCategory(), "getCategory");
        expectNPE("PrinterInfo(null, locale)", new Runnable() { public void run() { new PrinterInfo(null, java.util.Locale.FRENCH); } }, " value is null");
        PrinterInfo dl = new PrinterInfo("x", null);
        check(java.util.Locale.getDefault().equals(dl.getLocale()), "null locale -> default locale");
        check(v.equals(new PrinterInfo("contract", java.util.Locale.FRENCH)), "equals same value+locale");
        check(!v.equals(new PrinterInfo("contract", java.util.Locale.ENGLISH)), "different locale unequal");
        check(!v.equals(new PrinterInfo("other", java.util.Locale.FRENCH)), "different value unequal");
        check(!v.equals(null) && !v.equals("contract"), "null/foreign unequal");
        check(v.hashCode() == new PrinterInfo("contract", java.util.Locale.FRENCH).hashCode(), "hashCode consistent");
        PrinterInfo rt = (PrinterInfo) serRoundTrip(v);
        check(rt.equals(v) && "contract".equals(rt.getValue()) && java.util.Locale.FRENCH.equals(rt.getLocale()), "serialization round-trip");
        PrinterInfo en = new PrinterInfo("mixed", java.util.Locale.ENGLISH);
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
