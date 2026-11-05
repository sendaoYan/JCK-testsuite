/*
 * @test
 * @summary javax.print.attribute.EnumSyntax conformance: abstract enum syntax class.
 * @run main EnumSyntaxTest
 */

import java.lang.reflect.*;


public class EnumSyntaxTest {
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
        Class<?> c = Class.forName("javax.print.attribute.EnumSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        c.getDeclaredMethod("getStringTable");
        c.getDeclaredMethod("getEnumValueTable");
        c.getDeclaredMethod("getOffset");
        c.getDeclaredMethod("readResolve");
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
