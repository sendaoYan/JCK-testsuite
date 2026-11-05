/*
 * @test
 * @summary javax.print.DocFlavor.INPUT_STREAM conformance: doc flavor nested class.
 * @run main INPUT_STREAMTest
 */

import java.lang.reflect.*;


public class INPUT_STREAMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "INPUT_STREAM");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_TEXT_PLAIN_US_ASCII = javax.print.DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII;
        check("text/plain; charset=\"us-ascii\"".equals(f_TEXT_PLAIN_US_ASCII.getMimeType()), "TEXT_PLAIN_US_ASCII mimeType");
        check("java.io.InputStream".equals(f_TEXT_PLAIN_US_ASCII.getRepresentationClassName()), "TEXT_PLAIN_US_ASCII representation");
        javax.print.DocFlavor f_AUTOSENSE = javax.print.DocFlavor.INPUT_STREAM.AUTOSENSE;
        check("application/octet-stream".equals(f_AUTOSENSE.getMimeType()), "AUTOSENSE mimeType");
        check("java.io.InputStream".equals(f_AUTOSENSE.getRepresentationClassName()), "AUTOSENSE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.INPUT_STREAM("text/plain; charset=\"us-ascii\"");
        check(made instanceof javax.print.DocFlavor.INPUT_STREAM, "ctor instanceof");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full predefined constant table / rep-class family / mime spot checks ----
    static void testContractDeep() throws Exception {
        check(javax.print.DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_8.getRepresentationClassName().equals("java.io.InputStream"), "UTF_8 rep");
        check(javax.print.DocFlavor.INPUT_STREAM.AUTOSENSE.getRepresentationClassName().equals("java.io.InputStream"), "AUTOSENSE rep");
        check(javax.print.DocFlavor.INPUT_STREAM.PNG.getMimeType().equals("image/png"), "PNG mime");
        check(javax.print.DocFlavor.INPUT_STREAM.PDF.getMimeType().equals("application/pdf"), "PDF mime");
        javax.print.DocFlavor is = new javax.print.DocFlavor("application/pdf", "java.io.InputStream");
        check(is.equals(javax.print.DocFlavor.INPUT_STREAM.PDF), "constructed equals predefined PDF");
        check(!is.equals(javax.print.DocFlavor.BYTE_ARRAY.PDF), "InputStream != byte[] flavor for same mime");
        check(javax.print.DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII.getMimeType().equals("text/plain; charset=\"us-ascii\""), "US_ASCII mime");
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
