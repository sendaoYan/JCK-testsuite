/*
 * @test
 * @summary javax.print.attribute.HashDocAttributeSet conformance: white-box structure + black-box behavior.
 * @run main HashDocAttributeSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class HashDocAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.HashDocAttributeSet");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(HashAttributeSet.class.isAssignableFrom(c), "extends HashAttributeSet");
        check(DocAttributeSet.class.isAssignableFrom(c), "implements DocAttributeSet");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashDocAttributeSet s = new HashDocAttributeSet();
        check(s.isEmpty() && s.size() == 0, "empty");
        check(s.add(new DocumentName("n", Locale.ROOT)), "add returns changed");
        check(!s.add(new DocumentName("n", Locale.ROOT)), "re-add same returns false");
        check(s.size() == 1 && s.containsKey(DocumentName.class) && s.containsValue(new DocumentName("n", Locale.ROOT)), "contains");
        check(new DocumentName("n", Locale.ROOT).equals(s.get(DocumentName.class)), "get");
        s.add(new MediaPrintableArea(1, 1, 2, 2, MediaPrintableArea.INCH));
        check(s.size() == 2, "two attributes");
        Attribute[] arr = s.toArray();
        check(arr.length == 2, "toArray");
        s.remove(DocumentName.class);
        check(s.size() == 1 && !s.containsKey(DocumentName.class), "remove(Class)");
        s.clear();
        check(s.isEmpty(), "clear");
        HashDocAttributeSet other = new HashDocAttributeSet(new DocumentName("n", Locale.ROOT));
        check(!s.equals(other) && !s.equals(null) && !s.equals("x"), "equals");
        check(s.hashCode() == s.hashCode(), "hashCode");
        boolean threw = false; try { s.add(new Copies(2)); } catch (ClassCastException e) { threw = true; }
        check(threw, "wrong category throws ClassCastException");
        check(new HashDocAttributeSet(new DocumentName[]{ new DocumentName("n", Locale.ROOT) }).size() == 1, "array ctor");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: interface-typed CCE / ctor variants / serialization ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.HashDocAttributeSet s = new javax.print.attribute.HashDocAttributeSet();
        check(s instanceof javax.print.attribute.DocAttributeSet, "implements DocAttributeSet");
        s.add(javax.print.attribute.standard.Chromaticity.COLOR);
        check(s.size() == 1, "typed add OK");
        expectCCE("add foreign interface attribute", new Runnable() { public void run() { s.add((javax.print.attribute.Attribute) new javax.print.attribute.standard.JobName("x", null)); } });
        check(s.size() == 1, "failed add leaves size");
        javax.print.attribute.HashDocAttributeSet fromAttr = new javax.print.attribute.HashDocAttributeSet(javax.print.attribute.standard.Chromaticity.COLOR);
        check(fromAttr.size() == 1, "single-attribute ctor");
        javax.print.attribute.HashDocAttributeSet fromArr = new javax.print.attribute.HashDocAttributeSet(new javax.print.attribute.DocAttribute[] { javax.print.attribute.standard.Chromaticity.COLOR });
        check(fromArr.size() == 1, "array ctor");
        javax.print.attribute.HashDocAttributeSet fromSet = new javax.print.attribute.HashDocAttributeSet(s);
        check(fromSet.size() == 1, "set ctor");
        javax.print.attribute.HashDocAttributeSet ser = (javax.print.attribute.HashDocAttributeSet) serRoundTrip(s);
        check(ser.equals(s), "serialization round-trip");
        javax.print.attribute.HashAttributeSet plain = new javax.print.attribute.HashAttributeSet();
        plain.add(javax.print.attribute.standard.Chromaticity.COLOR);
        check(s.equals(plain), "content equality with untyped set");
        check(s.hashCode() == plain.hashCode(), "hashCode equality with untyped set");
        javax.print.attribute.AttributeSet un = javax.print.attribute.AttributeSetUtilities.unmodifiableView(s);
        try { un.clear(); throw new AssertionError("expected UnmodifiableSetException"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        s.clear();
        check(s.isEmpty(), "clear");
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
