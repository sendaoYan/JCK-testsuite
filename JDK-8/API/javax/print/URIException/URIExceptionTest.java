/*
 * @test
 * @summary javax.print.URIException conformance: interface.
 * @run main URIExceptionTest
 */

import java.lang.reflect.*;


public class URIExceptionTest {
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
        check(javax.print.URIException.URIInaccessible == 1, "URIInaccessible constant");
        check(javax.print.URIException.URISchemeNotSupported == 2, "URISchemeNotSupported constant");
        check(javax.print.URIException.URIOtherProblem == -1, "URIOtherProblem constant");
        Class<?> c = Class.forName("javax.print.URIException");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getUnsupportedURI"), "declares getUnsupportedURI");
        check(hasMethod(c, "getReason"), "declares getReason");
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
