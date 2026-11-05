/*
 * @test
 * @summary javax.print.attribute.standard.PrinterMoreInfoManufacturer conformance: URI attribute.
 * @run main PrinterMoreInfoManufacturerTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterMoreInfoManufacturerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterMoreInfoManufacturer v = new PrinterMoreInfoManufacturer(new java.net.URI("http://example.com/x"));
        check("http://example.com/x".equals(v.getURI().toString()), "getURI");
        check(v.getCategory() == PrinterMoreInfoManufacturer.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterMoreInfoManufacturer(new java.net.URI("http://example.com/x"))), "equals");
        check(v.hashCode() == new PrinterMoreInfoManufacturer(new java.net.URI("http://example.com/x")).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: uri identity / null NPE / equals / serialization ----
    static void testContractDeep() throws Exception {
        java.net.URI u = new java.net.URI("http://example.com/print/queue1");
        PrinterMoreInfoManufacturer v = new PrinterMoreInfoManufacturer(u);
        check(v.getURI() == u, "getURI returns same reference (no defensive copy)");
        check("http://example.com/print/queue1".equals(v.toString()), "toString == uri");
        check("printer-more-info-manufacturer".equals(v.getName()), "getName");
        check(PrinterMoreInfoManufacturer.class == v.getCategory(), "getCategory");
        expectNPE("PrinterMoreInfoManufacturer(null)", new Runnable() { public void run() { new PrinterMoreInfoManufacturer(null); } }, " uri is null");
        check(v.equals(new PrinterMoreInfoManufacturer(URI2)), "equals same uri");
        check(!v.equals(new PrinterMoreInfoManufacturer(new java.net.URI("http://example.com/other"))), "different uri unequal");
        check(!v.equals(null) && !v.equals(u), "null/foreign unequal");
        check(v.hashCode() == new PrinterMoreInfoManufacturer(URI2).hashCode(), "hashCode consistent");
        PrinterMoreInfoManufacturer rt = (PrinterMoreInfoManufacturer) serRoundTrip(v);
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
