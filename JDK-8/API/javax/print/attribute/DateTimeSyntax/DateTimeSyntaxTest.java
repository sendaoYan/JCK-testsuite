/*
 * @test
 * @summary javax.print.attribute.DateTimeSyntax conformance: abstract syntax class.
 * @run main DateTimeSyntaxTest
 */

import java.lang.reflect.*;


public class DateTimeSyntaxTest {
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
        Class<?> c = Class.forName("javax.print.attribute.DateTimeSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Date storage / defensive copy / null NPE ----
    static void testContractDeep() throws Exception {
        java.util.Date d = new java.util.Date(1793853296789L);
        MyDt t = new MyDt(d);
        check(t.getValue().getTime() == 1793853296789L, "millis preserved");
        check(t.getValue() != d, "getValue defensive copy");
        d.setTime(0L);
        check(t.getValue().getTime() == 0L, "ctor stores Date reference (source mutation visible)");
        java.util.Date d2 = new java.util.Date(1793853296789L);
        MyDt t2 = new MyDt(d2);
        java.util.Date got = t2.getValue();
        check(got != d2, "getValue returns fresh Date");
        got.setTime(0L);
        check(t2.getValue().getTime() == 1793853296789L, "returned Date mutation isolated (defensive copy per call)");
        expectNPE("MyDt(null)", new Runnable() { public void run() { new MyDt(null); } }, "value is null");
        check(t2.equals(new MyDt(new java.util.Date(1793853296789L))), "equals same instant");
        check(!t2.equals(new MyDt(new java.util.Date(1L))), "different instant unequal");
        check(!t2.equals(new java.util.Date(1793853296789L)), "foreign type unequal");
        check(t2.hashCode() == new MyDt(new java.util.Date(1793853296789L)).hashCode(), "hashCode");
        MyDt rt = (MyDt) serRoundTrip(t2);
        check(rt.equals(t2) && rt.getValue().getTime() == 1793853296789L, "serialization round-trip");
    }
    static class MyDt extends javax.print.attribute.DateTimeSyntax {
        MyDt(java.util.Date d) { super(d); }
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
