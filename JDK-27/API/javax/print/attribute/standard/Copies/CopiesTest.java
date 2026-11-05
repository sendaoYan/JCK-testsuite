/*
 * @test
 * @summary javax.print.attribute.standard.Copies conformance: white-box structure + black-box behavior.
 * @run main CopiesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class CopiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Copies");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(IntegerSyntax.class.isAssignableFrom(c), "extends IntegerSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Copies a = new Copies(5);
        check(a.getValue() == 5, "getValue");
        check(a.getCategory() == Copies.class, "getCategory");
        check("copies".equals(a.getName()), "getName");
        check("5".equals(a.toString()), "toString");
        check(a.equals(new Copies(5)), "equals same value");
        check(a.hashCode() == 5, "hashCode == value");
        check(!a.equals(new Copies(6)), "different value unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(new Copies(1).getValue() == 1, "lower bound ok");
        boolean threw = false; try { new Copies(0); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "Copies(0) throws IllegalArgumentException");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor range matrix / equals / serialization ----
    static void testContractDeep() throws Exception {
        check(new Copies(1).getValue() == 1, "min boundary 1 accepted");
        check(new Copies(2147483647).getValue() == 2147483647, "max boundary 2147483647 accepted");
        expectIAE("Copies(0)", new Runnable() { public void run() { new Copies(0); } }, "Value 0 not in range 1..2147483647");
        check(new Copies(Integer.MAX_VALUE).getValue() == Integer.MAX_VALUE, "Integer.MAX_VALUE accepted (no int overflow possible above)");
        expectIAE("Copies(Integer.MIN_VALUE)", new Runnable() { public void run() { new Copies(Integer.MIN_VALUE); } }, null);
        check("copies".equals(new Copies(1).getName()), "getName");
        check(Copies.class == new Copies(1).getCategory(), "getCategory");
        check(new Copies(7).equals(new Copies(7)), "equals same value");
        check(!new Copies(7).equals(new Copies(8)), "different value unequal");
        check(!new Copies(7).equals(null) && !new Copies(7).equals("7"), "null/foreign unequal");
        check(new Copies(7).hashCode() == 7, "hashCode == value");
        check(new Copies(7).equals(new javax.print.attribute.standard.Copies(7)) == ("Copies" == "Copies"), "cross IntegerSyntax subclass equals false");
        Copies rt = (Copies) serRoundTrip(new Copies(7));
        check(rt.equals(new Copies(7)) && rt.getValue() == 7, "serialization round-trip");
        check(new Copies(12).toString().equals("12"), "toString == value string");
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
