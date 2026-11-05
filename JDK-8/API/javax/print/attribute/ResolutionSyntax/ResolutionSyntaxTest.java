/*
 * @test
 * @summary javax.print.attribute.ResolutionSyntax conformance: abstract syntax class.
 * @run main ResolutionSyntaxTest
 */

import java.lang.reflect.*;


public class ResolutionSyntaxTest {
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
        check(javax.print.attribute.ResolutionSyntax.DPI == 100, "DPI constant");
        check(javax.print.attribute.ResolutionSyntax.DPCM == 254, "DPCM constant");
        Class<?> c = Class.forName("javax.print.attribute.ResolutionSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getResolution"), "declares getResolution");
        check(hasMethod(c, "getCrossFeedResolution"), "declares getCrossFeedResolution");
        check(hasMethod(c, "getFeedResolution"), "declares getFeedResolution");
        check(hasMethod(c, "toString"), "declares toString");
        check(hasMethod(c, "lessThanOrEquals"), "declares lessThanOrEquals");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: direct instantiation / dru normalization / physical equals / lte ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.ResolutionSyntax r = new MyRes(300, 600, javax.print.attribute.ResolutionSyntax.DPI);
        check(r.getCrossFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 300, "crossFeed getter");
        check(r.getFeedResolution(javax.print.attribute.ResolutionSyntax.DPI) == 600, "feed getter");
        check(r.toString().equals("30000x60000 dphi"), "toString dphi form");
        check(r.equals(new MyRes(150, 300, 200)), "physical equality unit scaling");
        check(r.hashCode() == new MyRes(150, 300, 200).hashCode(), "hashCode physical");
        check(r.lessThanOrEquals(r), "self lte");
        check(!r.lessThanOrEquals(new MyRes(150, 600, javax.print.attribute.ResolutionSyntax.DPI)), "lte false when crossFeed bigger");
        expectIAE("cross 0", new Runnable() { public void run() { new MyRes(0, 1, 100); } }, "crossFeedResolution is < 1");
        expectIAE("feed 0", new Runnable() { public void run() { new MyRes(1, 0, 100); } }, "feedResolution is < 1");
        expectIAE("units 0", new Runnable() { public void run() { new MyRes(1, 1, 0); } }, "units is < 1");
        javax.print.attribute.ResolutionSyntax rt = (javax.print.attribute.ResolutionSyntax) serRoundTrip(r);
        check(rt.equals(r), "serialization round-trip");
        check(!r.equals(null), "null unequal");
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

    static class MyRes extends javax.print.attribute.ResolutionSyntax {
        MyRes(int cf, int f, int u) { super(cf, f, u); }
    }

}
