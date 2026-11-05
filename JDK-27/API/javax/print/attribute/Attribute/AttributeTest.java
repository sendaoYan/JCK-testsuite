/*
 * @test
 * @summary javax.print.attribute.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.Attribute");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "getCategory"), "declares getCategory");
        check(hasMethod(c, "getName"), "declares getName");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Attribute a = new Copies(2);
        check(a.getCategory() == Copies.class && "copies".equals(a.getName()), "Attribute contract");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Attribute taxonomy / getCategory+getName contract / serialization ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.Attribute a = javax.print.attribute.standard.Copies.class.cast(new javax.print.attribute.standard.Copies(3));
        check(a.getCategory() == javax.print.attribute.standard.Copies.class, "getCategory == own class");
        check("copies".equals(a.getName()), "getName");
        check(a instanceof java.io.Serializable, "Serializable");
        check(javax.print.attribute.Attribute.class.isInterface(), "Attribute is interface");
        check(javax.print.attribute.Attribute.class.getMethod("getCategory").getReturnType() == Class.class, "getCategory signature");
        check(javax.print.attribute.Attribute.class.getMethod("getName").getReturnType() == String.class, "getName signature");
        Object rt = serRoundTrip(a);
        check(rt.equals(a), "serialization round-trip");
        // every standard attribute's getCategory equals its declaring class (spot matrix)
        javax.print.attribute.Attribute[] attrs = {
            javax.print.attribute.standard.Chromaticity.MONOCHROME, new javax.print.attribute.standard.Copies(1),
            new javax.print.attribute.standard.JobName("j", null), new javax.print.attribute.standard.PageRanges("1:2"),
            new javax.print.attribute.standard.Destination(java.net.URI.create("http://h/d")),
            javax.print.attribute.standard.PrinterState.IDLE };
        Class<?>[] cats = { javax.print.attribute.standard.Chromaticity.class, javax.print.attribute.standard.Copies.class,
            javax.print.attribute.standard.JobName.class, javax.print.attribute.standard.PageRanges.class,
            javax.print.attribute.standard.Destination.class, javax.print.attribute.standard.PrinterState.class };
        for (int i = 0; i < attrs.length; i++)
            check(attrs[i].getCategory() == cats[i], "category matrix " + cats[i].getSimpleName());
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
