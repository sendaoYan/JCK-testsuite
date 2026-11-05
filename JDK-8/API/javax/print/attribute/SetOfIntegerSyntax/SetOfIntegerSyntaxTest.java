/*
 * @test
 * @summary javax.print.attribute.SetOfIntegerSyntax conformance: abstract syntax class.
 * @run main SetOfIntegerSyntaxTest
 */

import java.lang.reflect.*;


public class SetOfIntegerSyntaxTest {
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
        Class<?> c = Class.forName("javax.print.attribute.SetOfIntegerSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getMembers"), "declares getMembers");
        check(hasMethod(c, "contains"), "declares contains");
        check(hasMethod(c, "next"), "declares next");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: all 4 protected ctors / parse matrix / next / equals / serialization ----
    static void testContractDeep() throws Exception {
        MySet s = new MySet("1:3,5,9:11");
        check(java.util.Arrays.deepEquals(s.getMembers(), new int[][] {{1,3},{5,5},{9,11}}), "string parse normalized");
        check("1-3,5,9-11".equals(s.toString()), "toString format");
        check(s.contains(1) && s.contains(11) && !s.contains(0) && !s.contains(12), "contains bounds");
        check(s.next(0) == 1 && s.next(3) == 5 && s.next(5) == 9 && s.next(11) == -1, "next strictly greater");
        check(java.util.Arrays.deepEquals(new MySet(new int[][] {{7,9},{1,2},{10,11}}).getMembers(), new int[][] {{1,2},{7,11}}), "array merge");
        check(java.util.Arrays.deepEquals(new MySet(6).getMembers(), new int[][] {{6,6}}), "single value");
        check(java.util.Arrays.deepEquals(new MySet(2, 4).getMembers(), new int[][] {{2,4}}), "range value");
        check(java.util.Arrays.deepEquals(new MySet("3:1").getMembers(), new int[0][]), "base String ctor silently drops reversed range (standard subclasses throw IAE)");
        expectIAE("bad word", new Runnable() { public void run() { new MySet("abc"); } }, null);
        check(java.util.Arrays.deepEquals(new MySet("").getMembers(), new int[0][]), "base String ctor accepts empty string -> empty set");
        check(java.util.Arrays.deepEquals(new MySet((int[][]) null).getMembers(), new int[0][]), "null int[][] -> empty set");
        check(java.util.Arrays.deepEquals(new MySet((String) null).getMembers(), new int[0][]), "null String -> empty set");
        expectIAE("negative range string", new Runnable() { public void run() { new MySet("-3:-1"); } }, null);
        expectIAE("negative range array", new Runnable() { public void run() { new MySet(new int[][] {{-3,-1}}); } }, null);
        check(java.util.Arrays.deepEquals(new MySet(9, 1).getMembers(), new int[0][]), "base (lo,hi) ctor with lo>hi silently yields empty set (standard subclasses throw)");
        check(s.equals(new MySet("9:11,1:3,5")), "equals order-insensitive");
        check(s.hashCode() == new MySet("9:11,1:3,5").hashCode(), "hashCode order-insensitive");
        check(!s.equals(new MySet("1:3")), "different members unequal");
        check(!s.equals("1:3"), "foreign type unequal");
        MySet rt = (MySet) serRoundTrip(s);
        check(rt.equals(s), "serialization round-trip");
        check(s.contains(new javax.print.attribute.standard.Copies(5)), "contains(IntegerSyntax) by value");
        check(!s.contains(new javax.print.attribute.standard.Copies(4)), "contains(IntegerSyntax) non-member");
    }
    static class MySet extends javax.print.attribute.SetOfIntegerSyntax {
        MySet(String s) { super(s); }
        MySet(int[][] m) { super(m); }
        MySet(int v) { super(v); }
        MySet(int lo, int hi) { super(lo, hi); }
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
