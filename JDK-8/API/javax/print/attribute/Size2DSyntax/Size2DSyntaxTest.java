/*
 * @test
 * @summary javax.print.attribute.Size2DSyntax conformance: abstract syntax class.
 * @run main Size2DSyntaxTest
 */

import java.lang.reflect.*;


public class Size2DSyntaxTest {
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
        check(javax.print.attribute.Size2DSyntax.INCH == 25400, "INCH constant");
        check(javax.print.attribute.Size2DSyntax.MM == 1000, "MM constant");
        Class<?> c = Class.forName("javax.print.attribute.Size2DSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getSize"), "declares getSize");
        check(hasMethod(c, "getX"), "declares getX");
        check(hasMethod(c, "getY"), "declares getY");
        check(hasMethod(c, "toString"), "declares toString");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: float/double both forms / negative IAE / equality in dRU ----
    static void testContractDeep() throws Exception {
        MySize s = new MySize(2.54f, 5.08f, 100); // 100 dRU per inch units == inch
        check(s.getX(100) == 2.54f && s.getY(100) == 5.08f, "getX/getY float form");
        check(Math.abs(s.getX(2540) - 0.1f) < 1e-5f, "cross-unit conversion (2540 dRU/inch)");
        float[] f = s.getSize(100);
        check(f.length == 2 && f[0] == 2.54f && f[1] == 5.08f, "getSize [w,h]");
        check(javax.print.attribute.Size2DSyntax.INCH == 25400 && javax.print.attribute.Size2DSyntax.MM == 1000, "INCH=25400 MM=1000 (micrometers)");
        check(s.equals(new MySize(2.54f, 5.08f, 100)), "equals same size");
        check(s.equals(new MySize(1.27f, 2.54f, 200)), "physical equality across units");
        check(!s.equals(new MySize(2.54f, 5.09f, 100)), "different size unequal");
        check(!s.equals("2.54x5.08"), "foreign unequal");
        expectIAE("negative x", new Runnable() { public void run() { new MySize(-1f, 1f, 100); } }, "x < 0");
        expectIAE("negative y", new Runnable() { public void run() { new MySize(1f, -1f, 100); } }, "y < 0");
        expectIAE("negative units", new Runnable() { public void run() { new MySize(1f, 1f, -1); } }, "units < 1");
        check(new MySize(0f, 0f, 100).getX(100) == 0f, "zero size accepted");
        MySize rt = (MySize) serRoundTrip(s);
        check(rt.equals(s), "serialization round-trip");
        check(s.toString(100, " u").endsWith(" u"), "toString units suffix");
    }
    static class MySize extends javax.print.attribute.Size2DSyntax {
        MySize(float x, float y, int u) { super(x, y, u); }
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
