/*
 * @test
 * @summary javax.print.DocFlavor.SERVICE_FORMATTED conformance: doc flavor nested class.
 * @run main SERVICE_FORMATTEDTest
 */

import java.lang.reflect.*;


public class SERVICE_FORMATTEDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "SERVICE_FORMATTED");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_PAGEABLE = javax.print.DocFlavor.SERVICE_FORMATTED.PAGEABLE;
        check("application/x-java-jvm-local-objectref".equals(f_PAGEABLE.getMimeType()), "PAGEABLE mimeType");
        check("java.awt.print.Pageable".equals(f_PAGEABLE.getRepresentationClassName()), "PAGEABLE representation");
        javax.print.DocFlavor f_PRINTABLE = javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        check("application/x-java-jvm-local-objectref".equals(f_PRINTABLE.getMimeType()), "PRINTABLE mimeType");
        check("java.awt.print.Printable".equals(f_PRINTABLE.getRepresentationClassName()), "PRINTABLE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.SERVICE_FORMATTED("application/x-java-jvm-local-objectref");
        check(made instanceof javax.print.DocFlavor.SERVICE_FORMATTED, "ctor instanceof");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full predefined constant table / rep-class family / mime spot checks ----
    static void testContractDeep() throws Exception {
        check(javax.print.DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE.getRepresentationClassName().equals("java.awt.image.renderable.RenderableImage"), "rep");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE.getRepresentationClassName().equals("java.awt.print.Printable"), "rep");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PAGEABLE.getRepresentationClassName().equals("java.awt.print.Pageable"), "rep");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE.getMimeType().equals("application/x-java-jvm-local-objectref"), "local objectref mime");
        javax.print.DocFlavor p = new javax.print.DocFlavor("application/x-java-jvm-local-objectref", "java.awt.print.Printable");
        check(p.equals(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE), "constructed equals predefined");
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
