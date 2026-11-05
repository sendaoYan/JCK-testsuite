/*
 * @test
 * @summary javax.print.attribute.URISyntax conformance: white-box structure + black-box behavior.
 * @run main URISyntaxTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class URISyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.URISyntax");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(java.io.Serializable.class.isAssignableFrom(c) && Cloneable.class.isAssignableFrom(c), "Serializable+Cloneable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.net.URI u = java.net.URI.create("ipp://h/p");
        URISyntax t = new PrinterURI(u);
        check(u.equals(t.getURI()) && u.toString().equals(t.toString()), "getURI/toString");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
