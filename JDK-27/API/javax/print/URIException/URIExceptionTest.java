/*
 * @test
 * @summary javax.print.URIException conformance: white-box structure + black-box behavior.
 * @run main URIExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class URIExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.URIException");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "getUnsupportedURI"), "declares getUnsupportedURI");
        check(hasMethod(c, "getReason"), "declares getReason");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(URIException.URIInaccessible == 1 && URIException.URISchemeNotSupported == 2 && URIException.URIOtherProblem == -1, "URIException constants");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording implementation of URIException + reason constants ----
    static void testContractDeep() throws Exception {
        check(javax.print.URIException.URIInaccessible == 1, "URIInaccessible == 1");
        check(javax.print.URIException.URISchemeNotSupported == 2, "URISchemeNotSupported == 2");
        check(javax.print.URIException.URIOtherProblem == -1, "URIOtherProblem == -1");
        java.net.URI u = java.net.URI.create("ipp://host/q");
        RecUriException e = new RecUriException(javax.print.URIException.URIOtherProblem, u);
        check(e.getUnsupportedURI() == u, "uri identity");
        check(e.getReason() == javax.print.URIException.URIOtherProblem, "reason identity");
        check(e instanceof javax.print.PrintException, "extends PrintException");
    }
    static class RecUriException extends javax.print.PrintException implements javax.print.URIException {
        final int reason; final java.net.URI uri;
        RecUriException(int r, java.net.URI u) { reason = r; uri = u; }
        public int getReason() { return reason; }
        public java.net.URI getUnsupportedURI() { return uri; }
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
