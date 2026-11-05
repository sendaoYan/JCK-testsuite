/*
 * @test
 * @summary javax.print.attribute.HashPrintJobAttributeSet conformance: attribute set implementation.
 * @run main HashPrintJobAttributeSetTest
 */

import java.lang.reflect.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class HashPrintJobAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        HashPrintJobAttributeSet set = new HashPrintJobAttributeSet();
        check(set.isEmpty(), "initially empty");
        HashPrintJobAttributeSet set2 = new HashPrintJobAttributeSet(new JobName("x", java.util.Locale.US));
        check(!set2.isEmpty(), "single-arg ctor");
        HashPrintJobAttributeSet set3 = new HashPrintJobAttributeSet(set2);
        check(set3.equals(set2), "copy ctor");
        check(set instanceof javax.print.attribute.PrintJobAttributeSet, "implements PrintJobAttributeSet");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: interface-typed CCE / ctor variants / serialization ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.HashPrintJobAttributeSet s = new javax.print.attribute.HashPrintJobAttributeSet();
        check(s instanceof javax.print.attribute.PrintJobAttributeSet, "implements PrintJobAttributeSet");
        s.add(new javax.print.attribute.standard.Copies(1));
        check(s.size() == 1, "typed add OK");
        expectCCE("add foreign interface attribute", new Runnable() { public void run() { s.add((javax.print.attribute.Attribute) new javax.print.attribute.standard.PrinterName("x", null)); } });
        check(s.size() == 1, "failed add leaves size");
        javax.print.attribute.HashPrintJobAttributeSet fromAttr = new javax.print.attribute.HashPrintJobAttributeSet(new javax.print.attribute.standard.Copies(1));
        check(fromAttr.size() == 1, "single-attribute ctor");
        javax.print.attribute.HashPrintJobAttributeSet fromArr = new javax.print.attribute.HashPrintJobAttributeSet(new javax.print.attribute.PrintJobAttribute[] { new javax.print.attribute.standard.Copies(1) });
        check(fromArr.size() == 1, "array ctor");
        javax.print.attribute.HashPrintJobAttributeSet fromSet = new javax.print.attribute.HashPrintJobAttributeSet(s);
        check(fromSet.size() == 1, "set ctor");
        javax.print.attribute.HashPrintJobAttributeSet ser = (javax.print.attribute.HashPrintJobAttributeSet) serRoundTrip(s);
        check(ser.equals(s), "serialization round-trip");
        javax.print.attribute.HashAttributeSet plain = new javax.print.attribute.HashAttributeSet();
        plain.add(new javax.print.attribute.standard.Copies(1));
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
