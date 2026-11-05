/*
 * @test
 * @summary javax.print.AttributeException conformance: white-box structure + black-box behavior.
 * @run main AttributeExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class AttributeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.AttributeException");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "getUnsupportedAttributes"), "declares getUnsupportedAttributes");
        check(hasMethod(c, "getUnsupportedValues"), "declares getUnsupportedValues");
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording implementation of AttributeException ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.Attribute[] values = { new javax.print.attribute.standard.Copies(2) };
        Class<?>[] cats = { javax.print.attribute.standard.Copies.class };
        RecAttrException e = new RecAttrException(cats, values);
        check(e.getUnsupportedValues() == values, "unsupported values array identity");
        check(e.getUnsupportedAttributes() == cats, "unsupported categories array identity");
        check(e.getUnsupportedValues()[0].equals(new javax.print.attribute.standard.Copies(2)), "value content");
        check(e.getUnsupportedAttributes()[0] == javax.print.attribute.standard.Copies.class, "category content");
        check(e instanceof javax.print.PrintException, "extends PrintException");
        javax.print.AttributeException nullAll = new RecAttrException(null, null);
        check(nullAll.getUnsupportedValues() == null && nullAll.getUnsupportedAttributes() == null, "null arrays allowed");
    }
    static class RecAttrException extends javax.print.PrintException implements javax.print.AttributeException {
        final Class<?>[] cats; final javax.print.attribute.Attribute[] values;
        RecAttrException(Class<?>[] c, javax.print.attribute.Attribute[] v) { cats = c; values = v; }
        public Class<?>[] getUnsupportedAttributes() { return cats; }
        public javax.print.attribute.Attribute[] getUnsupportedValues() { return values; }
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
