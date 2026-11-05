/*
 * @test
 * @summary javax.print.attribute.IntegerSyntax conformance: white-box structure + black-box behavior.
 * @run main IntegerSyntaxTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class IntegerSyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.IntegerSyntax");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(java.io.Serializable.class.isAssignableFrom(c) && Cloneable.class.isAssignableFrom(c), "Serializable+Cloneable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IntegerSyntax i = new Copies(5);
        check(i.getValue() == 5 && "5".equals(i.toString()), "getValue/toString");
        check(i.equals(new Copies(5)) && i.hashCode() == 5, "equals/hashCode");
        check(!i.equals(new Copies(6)), "different value unequal");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: custom subclass / equals class-check / serialization ----
    static void testContractDeep() throws Exception {
        MyInt a = new MyInt(42), b = new MyInt(42), c = new MyInt(43);
        check(a.getValue() == 42, "getValue");
        check(a.equals(b) && b.equals(a), "equals symmetric same value");
        check(a.hashCode() == 42 && b.hashCode() == 42, "hashCode == value");
        check(!a.equals(c), "different value unequal");
        check(!a.equals(null) && !a.equals("42"), "null/foreign unequal");
        check(a.equals(new javax.print.attribute.standard.Copies(42)), "base IntegerSyntax.equals is value-based (instanceof, no getClass) -> true");
        check(!new javax.print.attribute.standard.Copies(42).equals(a), "Copies.equals is getClass-strict -> asymmetric false");
        check("42".equals(a.toString()), "toString == value");
        MyInt rt = (MyInt) serRoundTrip(a);
        check(rt.equals(a) && rt.getValue() == 42, "custom IntegerSyntax serialization round-trip");
        javax.print.attribute.standard.Copies cp = new javax.print.attribute.standard.Copies(7);
        javax.print.attribute.standard.Copies cpr = (javax.print.attribute.standard.Copies) serRoundTrip(cp);
        check(cpr.equals(cp), "standard IntegerSyntax round-trip");
    }
    static class MyInt extends javax.print.attribute.IntegerSyntax {
        MyInt(int v) { super(v); }
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
