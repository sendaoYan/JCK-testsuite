/*
 * @test
 * @summary javax.print.DocFlavor.INPUT_STREAM conformance: white-box structure + black-box behavior.
 * @run main INPUT_STREAMTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class INPUT_STREAMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocFlavor$INPUT_STREAM");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(DocFlavor.class.isAssignableFrom(c), "extends DocFlavor");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST != null, "TEXT_PLAIN_HOST non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST.getRepresentationClassName()), "TEXT_PLAIN_HOST repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_8 != null, "TEXT_PLAIN_UTF_8 non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_8.getRepresentationClassName()), "TEXT_PLAIN_UTF_8 repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16 != null, "TEXT_PLAIN_UTF_16 non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16.getRepresentationClassName()), "TEXT_PLAIN_UTF_16 repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16BE != null, "TEXT_PLAIN_UTF_16BE non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16BE.getRepresentationClassName()), "TEXT_PLAIN_UTF_16BE repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16LE != null, "TEXT_PLAIN_UTF_16LE non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16LE.getRepresentationClassName()), "TEXT_PLAIN_UTF_16LE repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII != null, "TEXT_PLAIN_US_ASCII non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII.getRepresentationClassName()), "TEXT_PLAIN_US_ASCII repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_HTML_HOST != null, "TEXT_HTML_HOST non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_HTML_HOST.getRepresentationClassName()), "TEXT_HTML_HOST repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_8 != null, "TEXT_HTML_UTF_8 non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_8.getRepresentationClassName()), "TEXT_HTML_UTF_8 repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16 != null, "TEXT_HTML_UTF_16 non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16.getRepresentationClassName()), "TEXT_HTML_UTF_16 repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16BE != null, "TEXT_HTML_UTF_16BE non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16BE.getRepresentationClassName()), "TEXT_HTML_UTF_16BE repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16LE != null, "TEXT_HTML_UTF_16LE non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16LE.getRepresentationClassName()), "TEXT_HTML_UTF_16LE repr class");
        check(DocFlavor.INPUT_STREAM.TEXT_HTML_US_ASCII != null, "TEXT_HTML_US_ASCII non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.TEXT_HTML_US_ASCII.getRepresentationClassName()), "TEXT_HTML_US_ASCII repr class");
        check(DocFlavor.INPUT_STREAM.PDF != null, "PDF non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.PDF.getRepresentationClassName()), "PDF repr class");
        check(DocFlavor.INPUT_STREAM.POSTSCRIPT != null, "POSTSCRIPT non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.POSTSCRIPT.getRepresentationClassName()), "POSTSCRIPT repr class");
        check(DocFlavor.INPUT_STREAM.PCL != null, "PCL non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.PCL.getRepresentationClassName()), "PCL repr class");
        check(DocFlavor.INPUT_STREAM.GIF != null, "GIF non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.GIF.getRepresentationClassName()), "GIF repr class");
        check(DocFlavor.INPUT_STREAM.JPEG != null, "JPEG non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.JPEG.getRepresentationClassName()), "JPEG repr class");
        check(DocFlavor.INPUT_STREAM.PNG != null, "PNG non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.PNG.getRepresentationClassName()), "PNG repr class");
        check(DocFlavor.INPUT_STREAM.AUTOSENSE != null, "AUTOSENSE non-null");
        check("java.io.InputStream".equals(DocFlavor.INPUT_STREAM.AUTOSENSE.getRepresentationClassName()), "AUTOSENSE repr class");
        check("application/pdf".equals(DocFlavor.INPUT_STREAM.PDF.getMimeType()), "PDF mime exact");
        check(DocFlavor.INPUT_STREAM.PDF.equals(DocFlavor.INPUT_STREAM.PDF) && DocFlavor.INPUT_STREAM.PDF.hashCode() == DocFlavor.INPUT_STREAM.PDF.hashCode(), "PDF equals/hash");
        check(!DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST.equals(DocFlavor.INPUT_STREAM.AUTOSENSE), "distinct constants unequal");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
