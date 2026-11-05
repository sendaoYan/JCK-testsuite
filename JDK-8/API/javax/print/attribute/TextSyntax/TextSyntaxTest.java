/*
 * @test
 * @summary javax.print.attribute.TextSyntax conformance: abstract syntax class.
 * @run main TextSyntaxTest
 */

import java.lang.reflect.*;


public class TextSyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.TextSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "getLocale"), "declares getLocale");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "toString"), "declares toString");
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
