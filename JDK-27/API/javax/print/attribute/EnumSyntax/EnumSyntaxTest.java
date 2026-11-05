/*
 * @test
 * @summary javax.print.attribute.EnumSyntax conformance: white-box structure + black-box behavior.
 * @run main EnumSyntaxTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class EnumSyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.EnumSyntax");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(java.io.Serializable.class.isAssignableFrom(c) && Cloneable.class.isAssignableFrom(c), "Serializable+Cloneable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        EnumSyntax e = OrientationRequested.PORTRAIT;
        check(e.getValue() == 3 && "portrait".equals(e.toString()), "getValue/toString");
        check(e.clone() == e, "clone returns singleton");
        check(e.hashCode() == 3, "hashCode == value");
        check(e.equals(e) && !e.equals(OrientationRequested.LANDSCAPE), "identity equals");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: custom subclass / clone / serialization table rules ----
    static void testContractDeep() throws Exception {
        MyEnum e = new MyEnum(5);
        check(e.getValue() == 5, "custom getValue");
        check(e.toString() != null, "custom toString non-null (empty table tolerated)");
        check(e.clone() != null && e.clone() != e || e.clone() == e, "clone non-null");
        check(e.hashCode() == 5, "hashCode == value");
        check(!e.equals(new MyEnum(5)), "EnumSyntax does NOT override equals: identity semantics");
        check(!e.equals(new MyEnum(6)), "distinct objects unequal (identity)");
        check(e.equals(e), "self equality");
        check(javax.print.attribute.standard.Sides.DUPLEX == javax.print.attribute.standard.Sides.TWO_SIDED_LONG_EDGE, "standard alias constants share one singleton");
        check(!e.equals(new javax.print.attribute.standard.Copies(5)), "cross-class unequal");
        boolean invalidObject = false;
        try {
            serRoundTrip(e);
        } catch (java.io.InvalidObjectException ex) {
            invalidObject = true;
            check(ex.getMessage() != null && ex.getMessage().startsWith("Null enumeration value table"), "InvalidObjectException message [" + ex.getMessage() + "]");
        }
        check(invalidObject, "serialization of table-less EnumSyntax subclass rejected (InvalidObjectException)");
        javax.print.attribute.standard.Chromaticity mono = javax.print.attribute.standard.Chromaticity.MONOCHROME;
        check(serRoundTrip(mono) == mono, "registered enum singleton restored via readResolve");
    }
    static class MyEnum extends javax.print.attribute.EnumSyntax {
        MyEnum(int v) { super(v); }
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
