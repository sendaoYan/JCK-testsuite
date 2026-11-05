/*
 * @test
 * @summary javax.print.attribute.URISyntax conformance: abstract syntax class.
 * @run main URISyntaxTest
 */

import java.lang.reflect.*;


public class URISyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.URISyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getURI"), "declares getURI");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "toString"), "declares toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: URI identity / null NPE / serialization ----
    static void testContractDeep() throws Exception {
        java.net.URI u = java.net.URI.create("ipp://server/queue");
        MyUri t = new MyUri(u);
        check(t.getURI() == u, "getURI same reference (no copy)");
        check("ipp://server/queue".equals(t.toString()), "toString == uri string");
        expectNPE("MyUri(null)", new Runnable() { public void run() { new MyUri(null); } }, " uri is null");
        check(t.equals(new MyUri(java.net.URI.create("ipp://server/queue"))), "equals same uri");
        check(!t.equals(new MyUri(java.net.URI.create("ipp://other"))), "different uri unequal");
        check(!t.equals(u), "foreign type unequal");
        check(t.hashCode() == new MyUri(java.net.URI.create("ipp://server/queue")).hashCode(), "hashCode");
        MyUri rt = (MyUri) serRoundTrip(t);
        check(rt.equals(t) && rt.getURI().equals(u), "serialization round-trip");
        javax.print.attribute.standard.PrinterURI pu = new javax.print.attribute.standard.PrinterURI(u);
        check("printer-uri".equals(pu.getName()), "standard name");
    }
    static class MyUri extends javax.print.attribute.URISyntax {
        MyUri(java.net.URI u) { super(u); }
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
    static void expectCCE(String what, Runnable r) {
        try { r.run(); throw new AssertionError("expected CCE: " + what); }
        catch (ClassCastException e) { check(e.getMessage() == null, "CCE message null: [" + e.getMessage() + "]"); }
    }

}
