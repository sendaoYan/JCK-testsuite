/*
 * @test
 * @summary javax.print.DocFlavor.URL conformance: white-box structure + black-box behavior.
 * @run main URLTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class URLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocFlavor$URL");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(DocFlavor.class.isAssignableFrom(c), "extends DocFlavor");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DocFlavor.URL.TEXT_PLAIN_HOST != null, "TEXT_PLAIN_HOST non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_PLAIN_HOST.getRepresentationClassName()), "TEXT_PLAIN_HOST repr class");
        check(DocFlavor.URL.TEXT_PLAIN_UTF_8 != null, "TEXT_PLAIN_UTF_8 non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_PLAIN_UTF_8.getRepresentationClassName()), "TEXT_PLAIN_UTF_8 repr class");
        check(DocFlavor.URL.TEXT_PLAIN_UTF_16 != null, "TEXT_PLAIN_UTF_16 non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_PLAIN_UTF_16.getRepresentationClassName()), "TEXT_PLAIN_UTF_16 repr class");
        check(DocFlavor.URL.TEXT_PLAIN_UTF_16BE != null, "TEXT_PLAIN_UTF_16BE non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_PLAIN_UTF_16BE.getRepresentationClassName()), "TEXT_PLAIN_UTF_16BE repr class");
        check(DocFlavor.URL.TEXT_PLAIN_UTF_16LE != null, "TEXT_PLAIN_UTF_16LE non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_PLAIN_UTF_16LE.getRepresentationClassName()), "TEXT_PLAIN_UTF_16LE repr class");
        check(DocFlavor.URL.TEXT_PLAIN_US_ASCII != null, "TEXT_PLAIN_US_ASCII non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_PLAIN_US_ASCII.getRepresentationClassName()), "TEXT_PLAIN_US_ASCII repr class");
        check(DocFlavor.URL.TEXT_HTML_HOST != null, "TEXT_HTML_HOST non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_HTML_HOST.getRepresentationClassName()), "TEXT_HTML_HOST repr class");
        check(DocFlavor.URL.TEXT_HTML_UTF_8 != null, "TEXT_HTML_UTF_8 non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_HTML_UTF_8.getRepresentationClassName()), "TEXT_HTML_UTF_8 repr class");
        check(DocFlavor.URL.TEXT_HTML_UTF_16 != null, "TEXT_HTML_UTF_16 non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_HTML_UTF_16.getRepresentationClassName()), "TEXT_HTML_UTF_16 repr class");
        check(DocFlavor.URL.TEXT_HTML_UTF_16BE != null, "TEXT_HTML_UTF_16BE non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_HTML_UTF_16BE.getRepresentationClassName()), "TEXT_HTML_UTF_16BE repr class");
        check(DocFlavor.URL.TEXT_HTML_UTF_16LE != null, "TEXT_HTML_UTF_16LE non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_HTML_UTF_16LE.getRepresentationClassName()), "TEXT_HTML_UTF_16LE repr class");
        check(DocFlavor.URL.TEXT_HTML_US_ASCII != null, "TEXT_HTML_US_ASCII non-null");
        check("java.net.URL".equals(DocFlavor.URL.TEXT_HTML_US_ASCII.getRepresentationClassName()), "TEXT_HTML_US_ASCII repr class");
        check(DocFlavor.URL.PDF != null, "PDF non-null");
        check("java.net.URL".equals(DocFlavor.URL.PDF.getRepresentationClassName()), "PDF repr class");
        check(DocFlavor.URL.POSTSCRIPT != null, "POSTSCRIPT non-null");
        check("java.net.URL".equals(DocFlavor.URL.POSTSCRIPT.getRepresentationClassName()), "POSTSCRIPT repr class");
        check(DocFlavor.URL.PCL != null, "PCL non-null");
        check("java.net.URL".equals(DocFlavor.URL.PCL.getRepresentationClassName()), "PCL repr class");
        check(DocFlavor.URL.GIF != null, "GIF non-null");
        check("java.net.URL".equals(DocFlavor.URL.GIF.getRepresentationClassName()), "GIF repr class");
        check(DocFlavor.URL.JPEG != null, "JPEG non-null");
        check("java.net.URL".equals(DocFlavor.URL.JPEG.getRepresentationClassName()), "JPEG repr class");
        check(DocFlavor.URL.PNG != null, "PNG non-null");
        check("java.net.URL".equals(DocFlavor.URL.PNG.getRepresentationClassName()), "PNG repr class");
        check(DocFlavor.URL.AUTOSENSE != null, "AUTOSENSE non-null");
        check("java.net.URL".equals(DocFlavor.URL.AUTOSENSE.getRepresentationClassName()), "AUTOSENSE repr class");
        check("application/pdf".equals(DocFlavor.URL.PDF.getMimeType()), "PDF mime exact");
        check(DocFlavor.URL.PDF.equals(DocFlavor.URL.PDF) && DocFlavor.URL.PDF.hashCode() == DocFlavor.URL.PDF.hashCode(), "PDF equals/hash");
        check(!DocFlavor.URL.TEXT_PLAIN_HOST.equals(DocFlavor.URL.AUTOSENSE), "distinct constants unequal");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
