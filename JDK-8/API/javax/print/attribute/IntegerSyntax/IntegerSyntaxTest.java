/*
 * @test
 * @summary javax.print.attribute.IntegerSyntax conformance: abstract syntax class.
 * @run main IntegerSyntaxTest
 */

import java.lang.reflect.*;


public class IntegerSyntaxTest {
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
        Class<?> c = Class.forName("javax.print.attribute.IntegerSyntax");
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
