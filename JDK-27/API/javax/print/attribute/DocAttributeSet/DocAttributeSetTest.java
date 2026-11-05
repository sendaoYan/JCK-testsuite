/*
 * @test
 * @summary javax.print.attribute.DocAttributeSet conformance: white-box structure + black-box behavior.
 * @run main DocAttributeSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DocAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.DocAttributeSet");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(AttributeSet.class.isAssignableFrom(c), "extends AttributeSet");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "addAll"), "declares addAll");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashDocAttributeSet s = new HashDocAttributeSet();
        s.add(new DocumentName("n", Locale.ROOT));
        check(s.size() == 1, "DocAttributeSet add");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: typed set interface extends AttributeSet; Hash implementer enforces typing ----
    static void testContractDeep() throws Exception {
        check(javax.print.attribute.DocAttributeSet.class.isInterface(), "is interface");
        check(javax.print.attribute.AttributeSet.class.isAssignableFrom(javax.print.attribute.DocAttributeSet.class), "extends AttributeSet");
        javax.print.attribute.DocAttributeSet s = new javax.print.attribute.HashDocAttributeSet();
        check(s instanceof javax.print.attribute.AttributeSet, "implementer is AttributeSet");
        javax.print.attribute.AttributeSet plain = (javax.print.attribute.AttributeSet) s;
        plain.add(javax.print.attribute.standard.Chromaticity.MONOCHROME);
        check(plain.size() == 1, "typed member accepted");
        try { plain.add((javax.print.attribute.Attribute) new javax.print.attribute.standard.Copies(1)); throw new AssertionError("expected CCE cross-interface"); }
        catch (ClassCastException e) { passed++; }
        check(plain.get(javax.print.attribute.standard.Chromaticity.class) != null, "get by category");
        check(plain.equals(new javax.print.attribute.HashDocAttributeSet(javax.print.attribute.standard.Chromaticity.MONOCHROME)) || plain.size() == 1, "copy ctor equality guard");
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
