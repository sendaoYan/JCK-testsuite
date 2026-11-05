/*
 * @test
 * @summary javax.print.FlavorException conformance: white-box structure + black-box behavior.
 * @run main FlavorExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class FlavorExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.FlavorException");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "getUnsupportedFlavors"), "declares getUnsupportedFlavors");
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
