/*
 * @test
 * @summary javax.print.DocFlavor.BYTE_ARRAY conformance: white-box structure + black-box behavior.
 * @run main BYTE_ARRAYTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class BYTE_ARRAYTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocFlavor$BYTE_ARRAY");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(DocFlavor.class.isAssignableFrom(c), "extends DocFlavor");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST != null, "TEXT_PLAIN_HOST non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST.getRepresentationClassName()), "TEXT_PLAIN_HOST repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8 != null, "TEXT_PLAIN_UTF_8 non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8.getRepresentationClassName()), "TEXT_PLAIN_UTF_8 repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16 != null, "TEXT_PLAIN_UTF_16 non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16.getRepresentationClassName()), "TEXT_PLAIN_UTF_16 repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16BE != null, "TEXT_PLAIN_UTF_16BE non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16BE.getRepresentationClassName()), "TEXT_PLAIN_UTF_16BE repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16LE != null, "TEXT_PLAIN_UTF_16LE non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16LE.getRepresentationClassName()), "TEXT_PLAIN_UTF_16LE repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_US_ASCII != null, "TEXT_PLAIN_US_ASCII non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_US_ASCII.getRepresentationClassName()), "TEXT_PLAIN_US_ASCII repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_HTML_HOST != null, "TEXT_HTML_HOST non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_HOST.getRepresentationClassName()), "TEXT_HTML_HOST repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_8 != null, "TEXT_HTML_UTF_8 non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_8.getRepresentationClassName()), "TEXT_HTML_UTF_8 repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16 != null, "TEXT_HTML_UTF_16 non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16.getRepresentationClassName()), "TEXT_HTML_UTF_16 repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16BE != null, "TEXT_HTML_UTF_16BE non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16BE.getRepresentationClassName()), "TEXT_HTML_UTF_16BE repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16LE != null, "TEXT_HTML_UTF_16LE non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16LE.getRepresentationClassName()), "TEXT_HTML_UTF_16LE repr class");
        check(DocFlavor.BYTE_ARRAY.TEXT_HTML_US_ASCII != null, "TEXT_HTML_US_ASCII non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_US_ASCII.getRepresentationClassName()), "TEXT_HTML_US_ASCII repr class");
        check(DocFlavor.BYTE_ARRAY.PDF != null, "PDF non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.PDF.getRepresentationClassName()), "PDF repr class");
        check(DocFlavor.BYTE_ARRAY.POSTSCRIPT != null, "POSTSCRIPT non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.POSTSCRIPT.getRepresentationClassName()), "POSTSCRIPT repr class");
        check(DocFlavor.BYTE_ARRAY.PCL != null, "PCL non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.PCL.getRepresentationClassName()), "PCL repr class");
        check(DocFlavor.BYTE_ARRAY.GIF != null, "GIF non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.GIF.getRepresentationClassName()), "GIF repr class");
        check(DocFlavor.BYTE_ARRAY.JPEG != null, "JPEG non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.JPEG.getRepresentationClassName()), "JPEG repr class");
        check(DocFlavor.BYTE_ARRAY.PNG != null, "PNG non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.PNG.getRepresentationClassName()), "PNG repr class");
        check(DocFlavor.BYTE_ARRAY.AUTOSENSE != null, "AUTOSENSE non-null");
        check("[B".equals(DocFlavor.BYTE_ARRAY.AUTOSENSE.getRepresentationClassName()), "AUTOSENSE repr class");
        check("text/plain; charset=\"utf-8\"".equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8.getMimeType()), "TEXT_PLAIN_UTF_8 mime exact");
        check(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8.equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8) && DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8.hashCode() == DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8.hashCode(), "TEXT_PLAIN_UTF_8 equals/hash");
        check(!DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST.equals(DocFlavor.BYTE_ARRAY.AUTOSENSE), "distinct constants unequal");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
