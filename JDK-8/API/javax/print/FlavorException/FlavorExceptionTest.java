/*
 * @test
 * @summary javax.print.FlavorException conformance: interface.
 * @run main FlavorExceptionTest
 */

import java.lang.reflect.*;


public class FlavorExceptionTest {
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
        Class<?> c = Class.forName("javax.print.FlavorException");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getUnsupportedFlavors"), "declares getUnsupportedFlavors");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording implementation of FlavorException ----
    static void testContractDeep() throws Exception {
        javax.print.DocFlavor[] flavors = { javax.print.DocFlavor.BYTE_ARRAY.PNG };
        RecFlavorException e = new RecFlavorException(flavors);
        check(e.getUnsupportedFlavors() == flavors, "flavor array identity");
        check(e.getUnsupportedFlavors()[0].equals(javax.print.DocFlavor.BYTE_ARRAY.PNG), "content");
        check(e instanceof javax.print.PrintException, "extends PrintException");
        RecFlavorException n = new RecFlavorException(null);
        check(n.getUnsupportedFlavors() == null, "null array allowed");
    }
    static class RecFlavorException extends javax.print.PrintException implements javax.print.FlavorException {
        final javax.print.DocFlavor[] flavors;
        RecFlavorException(javax.print.DocFlavor[] f) { flavors = f; }
        public javax.print.DocFlavor[] getUnsupportedFlavors() { return flavors; }
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
