/*
 * @test
 * @summary javax.print.attribute.standard.PrinterURI conformance: URI attribute.
 * @run main PrinterURITest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterURITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterURI v = new PrinterURI(new java.net.URI("http://example.com/x"));
        check("http://example.com/x".equals(v.getURI().toString()), "getURI");
        check(v.getCategory() == PrinterURI.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterURI(new java.net.URI("http://example.com/x"))), "equals");
        check(v.hashCode() == new PrinterURI(new java.net.URI("http://example.com/x")).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: uri identity / null NPE / equals / serialization ----
    static void testContractDeep() throws Exception {
        java.net.URI u = new java.net.URI("http://example.com/print/queue1");
        PrinterURI v = new PrinterURI(u);
        check(v.getURI() == u, "getURI returns same reference (no defensive copy)");
        check("http://example.com/print/queue1".equals(v.toString()), "toString == uri");
        check("printer-uri".equals(v.getName()), "getName");
        check(PrinterURI.class == v.getCategory(), "getCategory");
        expectNPE("PrinterURI(null)", new Runnable() { public void run() { new PrinterURI(null); } }, " uri is null");
        check(v.equals(new PrinterURI(URI2)), "equals same uri");
        check(!v.equals(new PrinterURI(new java.net.URI("http://example.com/other"))), "different uri unequal");
        check(!v.equals(null) && !v.equals(u), "null/foreign unequal");
        check(v.hashCode() == new PrinterURI(URI2).hashCode(), "hashCode consistent");
        PrinterURI rt = (PrinterURI) serRoundTrip(v);
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
