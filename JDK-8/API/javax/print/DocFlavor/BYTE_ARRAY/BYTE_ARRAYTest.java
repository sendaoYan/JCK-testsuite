/*
 * @test
 * @summary javax.print.DocFlavor.BYTE_ARRAY conformance: doc flavor nested class.
 * @run main BYTE_ARRAYTest
 */

import java.lang.reflect.*;


public class BYTE_ARRAYTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "BYTE_ARRAY");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_PDF = javax.print.DocFlavor.BYTE_ARRAY.PDF;
        check("application/pdf".equals(f_PDF.getMimeType()), "PDF mimeType");
        check("[B".equals(f_PDF.getRepresentationClassName()), "PDF representation");
        javax.print.DocFlavor f_TEXT_PLAIN_UTF_8 = javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8;
        check("text/plain; charset=\"utf-8\"".equals(f_TEXT_PLAIN_UTF_8.getMimeType()), "TEXT_PLAIN_UTF_8 mimeType");
        check("[B".equals(f_TEXT_PLAIN_UTF_8.getRepresentationClassName()), "TEXT_PLAIN_UTF_8 representation");
        javax.print.DocFlavor f_AUTOSENSE = javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE;
        check("application/octet-stream".equals(f_AUTOSENSE.getMimeType()), "AUTOSENSE mimeType");
        check("[B".equals(f_AUTOSENSE.getRepresentationClassName()), "AUTOSENSE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.BYTE_ARRAY("application/pdf");
        check(made instanceof javax.print.DocFlavor.BYTE_ARRAY, "ctor instanceof");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full predefined constant table / rep-class family / mime spot checks ----
    static void testContractDeep() throws Exception {
        javax.print.DocFlavor[] all = { javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST, javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8,
            javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16, javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16BE,
            javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16LE, javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_US_ASCII,
            javax.print.DocFlavor.BYTE_ARRAY.TEXT_HTML_HOST, javax.print.DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_8,
            javax.print.DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16, javax.print.DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16BE,
            javax.print.DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16LE, javax.print.DocFlavor.BYTE_ARRAY.TEXT_HTML_US_ASCII,
            javax.print.DocFlavor.BYTE_ARRAY.PDF, javax.print.DocFlavor.BYTE_ARRAY.POSTSCRIPT, javax.print.DocFlavor.BYTE_ARRAY.PCL,
            javax.print.DocFlavor.BYTE_ARRAY.GIF, javax.print.DocFlavor.BYTE_ARRAY.JPEG, javax.print.DocFlavor.BYTE_ARRAY.PNG,
            javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE };
        for (javax.print.DocFlavor f : all) check("[B".equals(f.getRepresentationClassName()), "byte[] rep " + f);
        check(all.length == 19, "19 byte-array constants");
        check(javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8.getMimeType().equals("text/plain; charset=\"utf-8\""), "UTF_8 mime");
        check(javax.print.DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16BE.getMimeType().equals("text/html; charset=\"utf-16be\""), "HTML UTF_16BE mime");
        check(javax.print.DocFlavor.BYTE_ARRAY.JPEG.getMimeType().equals("image/jpeg"), "JPEG mime");
        javax.print.DocFlavor h = new javax.print.DocFlavor("image/png", "[B");
        check(h.equals(javax.print.DocFlavor.BYTE_ARRAY.PNG), "constructed equals predefined PNG");
        java.util.Set<String> mimes = new java.util.HashSet<String>();
        for (javax.print.DocFlavor f : all) mimes.add(f.getMimeType());
        check(mimes.size() >= 14, "distinct mimes (host variants may coincide)");
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
