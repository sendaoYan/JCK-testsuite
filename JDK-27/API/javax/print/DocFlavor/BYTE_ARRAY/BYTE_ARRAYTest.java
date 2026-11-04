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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
