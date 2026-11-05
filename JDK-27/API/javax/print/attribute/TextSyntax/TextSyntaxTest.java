/*
 * @test
 * @summary javax.print.attribute.TextSyntax conformance: white-box structure + black-box behavior.
 * @run main TextSyntaxTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class TextSyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.TextSyntax");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(java.io.Serializable.class.isAssignableFrom(c) && Cloneable.class.isAssignableFrom(c), "Serializable+Cloneable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TextSyntax t = new DocumentName("x", Locale.ROOT);
        check("x".equals(t.getValue()) && Locale.ROOT.equals(t.getLocale()), "getValue/getLocale");
        check("x".equals(t.toString()), "toString");
        check(t.equals(new DocumentName("x", Locale.ROOT)), "equals");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: custom subclass / null NPE / locale handling ----
    static void testContractDeep() throws Exception {
        MyText t = new MyText("value1", java.util.Locale.GERMAN);
        check("value1".equals(t.getValue()), "getValue");
        check(java.util.Locale.GERMAN.equals(t.getLocale()), "getLocale");
        check("value1".equals(t.toString()), "toString == value");
        check(t.equals(new MyText("value1", java.util.Locale.GERMAN)), "equals same");
        check(!t.equals(new MyText("value1", java.util.Locale.FRENCH)), "different locale unequal");
        check(!t.equals(new MyText("value2", java.util.Locale.GERMAN)), "different value unequal");
        check(t.hashCode() == new MyText("value1", java.util.Locale.GERMAN).hashCode(), "hashCode consistent");
        expectNPE("MyText(null, locale)", new Runnable() { public void run() { new MyText(null, java.util.Locale.GERMAN); } }, " value is null");
        check(java.util.Locale.getDefault().equals(new MyText("x", null).getLocale()), "null locale -> default");
        MyText rt = (MyText) serRoundTrip(t);
        check(rt.equals(t) && rt.getLocale().equals(java.util.Locale.GERMAN), "serialization round-trip");
        javax.print.attribute.standard.JobName jn = new javax.print.attribute.standard.JobName("doc", null);
        check(jn.equals(new javax.print.attribute.standard.JobName("doc", null)), "standard TextSyntax equality");
    }
    static class MyText extends javax.print.attribute.TextSyntax {
        MyText(String v, java.util.Locale l) { super(v, l); }
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
