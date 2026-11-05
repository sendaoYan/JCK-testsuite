/*
 * @test
 * @summary javax.print.Doc conformance: white-box structure + black-box behavior.
 * @run main DocTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DocTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.Doc");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "getDocFlavor"), "declares getDocFlavor");
        check(hasMethod(c, "getPrintData"), "declares getPrintData");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getReaderForText"), "declares getReaderForText");
        check(hasMethod(c, "getStreamForBytes"), "declares getStreamForBytes");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SimpleDoc d = new SimpleDoc("x", DocFlavor.STRING.TEXT_PLAIN, null);
        check(DocFlavor.STRING.TEXT_PLAIN.equals(d.getDocFlavor()) && "x".equals(d.getPrintData()), "Doc contract");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Doc contract via SimpleDoc + recording implementation ----
    static void testContractDeep() throws Exception {
        javax.print.Doc d = new javax.print.SimpleDoc("doc-body", javax.print.DocFlavor.STRING.TEXT_PLAIN, null);
        check(d.getDocFlavor().equals(javax.print.DocFlavor.STRING.TEXT_PLAIN), "flavor");
        check("doc-body".equals(d.getPrintData()), "data");
        check(d.getAttributes() == null, "SimpleDoc null attrs passthrough");
        RecDoc rec = new RecDoc();
        check(rec.getDocFlavor() == FLAVOR, "custom doc flavor identity");
        check(rec.getPrintData().equals("payload"), "custom doc data");
        check(rec.getAttributes() == null, "custom doc null attrs allowed by interface");
    }
    static final javax.print.DocFlavor FLAVOR = javax.print.DocFlavor.STRING.TEXT_PLAIN;
    static class RecDoc implements javax.print.Doc {
        public javax.print.DocFlavor getDocFlavor() { return FLAVOR; }
        public java.lang.Object getPrintData() { return "payload"; }
        public javax.print.attribute.DocAttributeSet getAttributes() { return null; }
        public java.io.Reader getReaderForText() { return new java.io.StringReader("payload"); }
        public java.io.InputStream getStreamForBytes() { return null; }
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
