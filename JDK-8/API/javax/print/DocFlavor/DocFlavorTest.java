/*
 * @test
 * @summary javax.print.DocFlavor conformance: doc flavor class.
 * @run main DocFlavorTest
 */

import java.lang.reflect.*;
import javax.print.DocFlavor;

public class DocFlavorTest {
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
        DocFlavor f = new DocFlavor("text/plain; charset=utf-8", "java.lang.String");
        check("text".equals(f.getMediaType()), "getMediaType");
        check("plain".equals(f.getMediaSubtype()), "getMediaSubtype");
        check("java.lang.String".equals(f.getRepresentationClassName()), "getRepresentationClassName");
        check("utf-8".equals(f.getParameter("charset")), "getParameter");
        check(f.getMimeType() != null && f.getMimeType().length() > 0, "getMimeType non-empty");
        DocFlavor f2 = new DocFlavor("text/plain; charset=utf-8", "java.lang.String");
        check(f.equals(f2) && f.hashCode() == f2.hashCode(), "equals/hashCode");
        check(f.toString() != null, "toString");
        check(javax.print.DocFlavor.hostEncoding != null, "hostEncoding");
        Class<?> c = Class.forName("javax.print.DocFlavor");
        check(hasIf(c, "java.io.Serializable"), "Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "Cloneable");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: mime normalization / charset case-insensitive equals / parse matrix / serialization ----
    static void testContractDeep() throws Exception {
        javax.print.DocFlavor f1 = new javax.print.DocFlavor("text/plain; charset=us-ascii", "java.lang.String");
        check("text/plain; charset=\"us-ascii\"".equals(f1.getMimeType()), "mime normalized + charset quoted [" + f1.getMimeType() + "]");
        javax.print.DocFlavor f2 = new javax.print.DocFlavor("  TEXT/PLAIN ;  CharSet = US-ASCII ", "java.lang.String");
        check(f1.equals(f2) && f2.equals(f1), "charset case-insensitive equality");
        check(f1.hashCode() == f2.hashCode(), "hashCode consistent with equals");
        check("text/plain; charset=\"us-ascii\"".equals(f2.getMimeType()), "upper-case input normalized");
        check("text".equals(f1.getMediaType()) && "plain".equals(f1.getMediaSubtype()), "media type/subtype");
        check("us-ascii".equals(f1.getParameter("charset")), "getParameter charset normalized");
        check(f1.getParameter("bogus") == null, "unknown parameter null");
        check("java.lang.String".equals(f1.getRepresentationClassName()), "representation class name");
        check(f1.equals(serRoundTrip(f1)), "serialization round-trip (f2 equal)");
        javax.print.DocFlavor extra = new javax.print.DocFlavor("text/plain; charset=us-ascii; x=y", "java.lang.String");
        check(!f1.equals(extra), "extra parameter makes different flavor");
        check(extra.getParameter("x").equals("y"), "extra parameter readable");
        expectNPE("null mime", new Runnable() { public void run() { new javax.print.DocFlavor(null, "java.lang.String"); } }, null);
        expectNPE("null class", new Runnable() { public void run() { new javax.print.DocFlavor("text/plain", null); } }, null);
        expectIAE("no slash", new Runnable() { public void run() { new javax.print.DocFlavor("nonsense", "java.lang.String"); } }, null);
        expectIAE("empty mime", new Runnable() { public void run() { new javax.print.DocFlavor("", "java.lang.String"); } }, null);
        expectIAE("lone slash", new Runnable() { public void run() { new javax.print.DocFlavor("/", "java.lang.String"); } }, null);
        javax.print.DocFlavor unknownClass = new javax.print.DocFlavor("text/plain", "no.such.Clazz");
        check("no.such.Clazz".equals(unknownClass.getRepresentationClassName()), "unknown representation class name accepted");
        // predefined constant tables
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE.getRepresentationClassName().equals("java.awt.print.Printable"), "PRINTABLE rep class");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PAGEABLE.getRepresentationClassName().equals("java.awt.print.Pageable"), "PAGEABLE rep class");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE.getRepresentationClassName().equals("java.awt.image.renderable.RenderableImage"), "RENDERABLE_IMAGE rep class");
        check("application/octet-stream".equals(javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE.getMimeType()), "AUTOSENSE mime");
        check("image/png".equals(javax.print.DocFlavor.BYTE_ARRAY.PNG.getMimeType()) && "[B".equals(javax.print.DocFlavor.BYTE_ARRAY.PNG.getRepresentationClassName()), "PNG byte[]");
        check("image/gif".equals(javax.print.DocFlavor.URL.GIF.getMimeType()) && "java.net.URL".equals(javax.print.DocFlavor.URL.GIF.getRepresentationClassName()), "GIF URL");
        check("application/pdf".equals(javax.print.DocFlavor.INPUT_STREAM.PDF.getMimeType()) && "java.io.InputStream".equals(javax.print.DocFlavor.INPUT_STREAM.PDF.getRepresentationClassName()), "PDF InputStream");
        check("application/postscript".equals(javax.print.DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType()), "POSTSCRIPT mime");
        check("text/html".equals(javax.print.DocFlavor.STRING.TEXT_HTML.getMediaType() + "/" + javax.print.DocFlavor.STRING.TEXT_HTML.getMediaSubtype()), "STRING TEXT_HTML media");
        check("java.lang.String".equals(javax.print.DocFlavor.STRING.TEXT_PLAIN.getRepresentationClassName()), "STRING rep");
        check("[C".equals(javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN.getRepresentationClassName()), "char[] rep");
        check("java.io.Reader".equals(javax.print.DocFlavor.READER.TEXT_HTML.getRepresentationClassName()), "Reader rep");
        check(javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST.getMimeType().equals("text/plain; charset=\"utf-8\""), "HOST flavor mirrors file.encoding utf-8 on this host");
        check(javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST.equals(javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8), "on utf-8 host HOST == UTF_8");
        javax.print.DocFlavor uni = new javax.print.DocFlavor("text/plain; charset=unicode", "java.lang.String");
        check(uni.getMimeType().equals("text/plain; charset=\"unicode\""), "unicode alias not normalized");
        check(!uni.equals(new javax.print.DocFlavor("text/plain; charset=utf-16", "java.lang.String")), "unicode != utf-16 flavor");
        // binary/host-independent constants
        String[] binaries = {"PDF", "POSTSCRIPT", "PCL", "GIF", "JPEG", "PNG", "AUTOSENSE"};
        javax.print.DocFlavor[] byteFlavors = { javax.print.DocFlavor.BYTE_ARRAY.PDF, javax.print.DocFlavor.BYTE_ARRAY.POSTSCRIPT,
            javax.print.DocFlavor.BYTE_ARRAY.PCL, javax.print.DocFlavor.BYTE_ARRAY.GIF, javax.print.DocFlavor.BYTE_ARRAY.JPEG,
            javax.print.DocFlavor.BYTE_ARRAY.PNG, javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE };
        for (int i = 0; i < byteFlavors.length; i++) {
            check(byteFlavors[i].getRepresentationClassName().equals("[B"), binaries[i] + " is byte[] flavor");
        }
        // charset param invariance across access forms
        check(javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_US_ASCII.getParameter("charset").equalsIgnoreCase("us-ascii"), "US_ASCII charset");
        check(javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16LE.getParameter("charset").equalsIgnoreCase("utf-16le"), "UTF_16LE charset");
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
