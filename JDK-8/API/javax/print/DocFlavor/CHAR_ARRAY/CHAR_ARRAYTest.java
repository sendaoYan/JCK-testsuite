/*
 * @test
 * @summary javax.print.DocFlavor.CHAR_ARRAY conformance: doc flavor nested class.
 * @run main CHAR_ARRAYTest
 */

import java.lang.reflect.*;


public class CHAR_ARRAYTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "CHAR_ARRAY");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_TEXT_PLAIN = javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN;
        check("text/plain; charset=\"utf-16\"".equals(f_TEXT_PLAIN.getMimeType()), "TEXT_PLAIN mimeType");
        check("[C".equals(f_TEXT_PLAIN.getRepresentationClassName()), "TEXT_PLAIN representation");
        javax.print.DocFlavor f_TEXT_HTML = javax.print.DocFlavor.CHAR_ARRAY.TEXT_HTML;
        check("text/html; charset=\"utf-16\"".equals(f_TEXT_HTML.getMimeType()), "TEXT_HTML mimeType");
        check("[C".equals(f_TEXT_HTML.getRepresentationClassName()), "TEXT_HTML representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.CHAR_ARRAY("text/plain; charset=\"utf-16\"");
        check(made instanceof javax.print.DocFlavor.CHAR_ARRAY, "ctor instanceof");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full predefined constant table / rep-class family / mime spot checks ----
    static void testContractDeep() throws Exception {
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN.getRepresentationClassName().equals("[C"), "TEXT_PLAIN rep [C");
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_HTML.getRepresentationClassName().equals("[C"), "TEXT_HTML rep [C");
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN.getMimeType().equals("text/plain; charset=\"utf-16\""), "predefined uses utf-16 mime");
        check(javax.print.DocFlavor.CHAR_ARRAY.TEXT_HTML.getMimeType().equals("text/html; charset=\"utf-16\""), "TEXT_HTML utf-16 mime");
        javax.print.DocFlavor c = new javax.print.DocFlavor("text/plain; charset=utf-16", "[C");
        check(c.equals(javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN), "constructed equals predefined");
        check(!c.equals(javax.print.DocFlavor.STRING.TEXT_PLAIN), "char[] != String flavor");
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
