/*
 * @test
 * @summary javax.print.attribute.standard.PrinterMoreInfo conformance: white-box structure + black-box behavior.
 * @run main PrinterMoreInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterMoreInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterMoreInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(URISyntax.class.isAssignableFrom(c), "extends URISyntax");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.net.URI u = java.net.URI.create("ipp://host/printer");
        PrinterMoreInfo a = new PrinterMoreInfo(u);
        check(u.equals(a.getURI()), "getURI");
        check("printer-more-info".equals(a.getName()), "getName");
        check(a.getCategory() == PrinterMoreInfo.class, "getCategory");
        check(u.toString().equals(a.toString()), "toString == uri");
        check(a.equals(new PrinterMoreInfo(java.net.URI.create("ipp://host/printer"))), "equals same uri");
        check(!a.equals(new PrinterMoreInfo(java.net.URI.create("ipp://other/printer"))), "different uri unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(a.hashCode() == new PrinterMoreInfo(java.net.URI.create("ipp://host/printer")).hashCode(), "hashCode consistent");
        boolean threw = false; try { new PrinterMoreInfo(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null uri throws NPE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: uri identity / null NPE / equals / serialization ----
    static void testContractDeep() throws Exception {
        java.net.URI u = new java.net.URI("http://example.com/print/queue1");
        PrinterMoreInfo v = new PrinterMoreInfo(u);
        check(v.getURI() == u, "getURI returns same reference (no defensive copy)");
        check("http://example.com/print/queue1".equals(v.toString()), "toString == uri");
        check("printer-more-info".equals(v.getName()), "getName");
        check(PrinterMoreInfo.class == v.getCategory(), "getCategory");
        expectNPE("PrinterMoreInfo(null)", new Runnable() { public void run() { new PrinterMoreInfo(null); } }, " uri is null");
        check(v.equals(new PrinterMoreInfo(URI2)), "equals same uri");
        check(!v.equals(new PrinterMoreInfo(new java.net.URI("http://example.com/other"))), "different uri unequal");
        check(!v.equals(null) && !v.equals(u), "null/foreign unequal");
        check(v.hashCode() == new PrinterMoreInfo(URI2).hashCode(), "hashCode consistent");
        PrinterMoreInfo rt = (PrinterMoreInfo) serRoundTrip(v);
        check(rt.equals(v) && rt.getURI().equals(u), "serialization round-trip");
    }
    static final java.net.URI URI2 = java.net.URI.create("http://example.com/print/queue1");

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
