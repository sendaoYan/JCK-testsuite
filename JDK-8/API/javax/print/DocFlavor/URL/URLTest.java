/*
 * @test
 * @summary javax.print.DocFlavor.URL conformance: doc flavor nested class.
 * @run main URLTest
 */

import java.lang.reflect.*;


public class URLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "URL");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_PDF = javax.print.DocFlavor.URL.PDF;
        check("application/pdf".equals(f_PDF.getMimeType()), "PDF mimeType");
        check("java.net.URL".equals(f_PDF.getRepresentationClassName()), "PDF representation");
        javax.print.DocFlavor f_AUTOSENSE = javax.print.DocFlavor.URL.AUTOSENSE;
        check("application/octet-stream".equals(f_AUTOSENSE.getMimeType()), "AUTOSENSE mimeType");
        check("java.net.URL".equals(f_AUTOSENSE.getRepresentationClassName()), "AUTOSENSE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.URL("application/pdf");
        check(made instanceof javax.print.DocFlavor.URL, "ctor instanceof");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full predefined constant table / rep-class family / mime spot checks ----
    static void testContractDeep() throws Exception {
        check(javax.print.DocFlavor.URL.TEXT_PLAIN_UTF_8.getRepresentationClassName().equals("java.net.URL"), "rep class");
        check(javax.print.DocFlavor.URL.GIF.getMimeType().equals("image/gif"), "GIF mime");
        check(javax.print.DocFlavor.URL.POSTSCRIPT.getMimeType().equals("application/postscript"), "PS mime");
        javax.print.DocFlavor u = new javax.print.DocFlavor("image/gif", "java.net.URL");
        check(u.equals(javax.print.DocFlavor.URL.GIF), "constructed equals predefined GIF");
        check(!u.equals(javax.print.DocFlavor.BYTE_ARRAY.GIF), "URL != byte[] flavor");
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
