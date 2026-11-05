/*
 * @test
 * @summary javax.print.MultiDoc conformance: interface.
 * @run main MultiDocTest
 */

import java.lang.reflect.*;


public class MultiDocTest {
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
        Class<?> c = Class.forName("javax.print.MultiDoc");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getDoc"), "declares getDoc");
        check(hasMethod(c, "next"), "declares next");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: chained MultiDoc contract ----
    static void testContractDeep() throws Exception {
        javax.print.SimpleDoc d1 = new javax.print.SimpleDoc("one", javax.print.DocFlavor.STRING.TEXT_PLAIN, null);
        javax.print.SimpleDoc d2 = new javax.print.SimpleDoc("two", javax.print.DocFlavor.STRING.TEXT_PLAIN, null);
        javax.print.MultiDoc last = new RecMultiDoc(d2, null);
        javax.print.MultiDoc first = new RecMultiDoc(d1, last);
        check(first.getDoc() == d1, "first doc identity");
        check(first.next() == last, "next identity");
        check(last.next() == null, "tail next null");
        check(last.getDoc() == d2, "tail doc");
    }
    static class RecMultiDoc implements javax.print.MultiDoc {
        final javax.print.Doc doc; final javax.print.MultiDoc next;
        RecMultiDoc(javax.print.Doc d, javax.print.MultiDoc n) { doc = d; next = n; }
        public javax.print.Doc getDoc() { return doc; }
        public javax.print.MultiDoc next() { return next; }
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
