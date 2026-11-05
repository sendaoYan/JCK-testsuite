/*
 * @test
 * @summary javax.print.DocFlavor.CHAR_ARRAY conformance: white-box structure + black-box behavior.
 * @run main CHAR_ARRAYTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class CHAR_ARRAYTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocFlavor$CHAR_ARRAY");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(DocFlavor.class.isAssignableFrom(c), "extends DocFlavor");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DocFlavor.CHAR_ARRAY.TEXT_PLAIN != null, "TEXT_PLAIN non-null");
        check("[C".equals(DocFlavor.CHAR_ARRAY.TEXT_PLAIN.getRepresentationClassName()), "TEXT_PLAIN repr class");
        check(DocFlavor.CHAR_ARRAY.TEXT_HTML != null, "TEXT_HTML non-null");
        check("[C".equals(DocFlavor.CHAR_ARRAY.TEXT_HTML.getRepresentationClassName()), "TEXT_HTML repr class");
        check("text/plain; charset=\"utf-16\"".equals(DocFlavor.CHAR_ARRAY.TEXT_PLAIN.getMimeType()), "TEXT_PLAIN mime exact");
        check(DocFlavor.CHAR_ARRAY.TEXT_PLAIN.equals(DocFlavor.CHAR_ARRAY.TEXT_PLAIN) && DocFlavor.CHAR_ARRAY.TEXT_PLAIN.hashCode() == DocFlavor.CHAR_ARRAY.TEXT_PLAIN.hashCode(), "TEXT_PLAIN equals/hash");
        check(!DocFlavor.CHAR_ARRAY.TEXT_PLAIN.equals(DocFlavor.CHAR_ARRAY.TEXT_HTML), "distinct constants unequal");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full predefined constant table / rep-class family / mime spot checks ----
    static void testContractDeep() throws Exception {
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN.getRepresentationClassName().equals("[C"), "TEXT_PLAIN rep [C");
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_HTML.getRepresentationClassName().equals("[C"), "TEXT_HTML rep [C");
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN.getMimeType().equals("text/plain; charset=\"utf-16\""), "predefined uses utf-16 mime");
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_HTML.getMimeType().equals("text/html; charset=\"utf-16\""), "TEXT_HTML utf-16 mime");
        javax.print.DocFlavor c = new javax.print.DocFlavor("text/plain; charset=utf-16", "[C");
        check(c.equals(javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN), "constructed equals predefined");
        check(!c.equals(javax.print.DocFlavor.STRING.TEXT_PLAIN), "char[] != String flavor");
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
