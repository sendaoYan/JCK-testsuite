/*
 * @test
 * @summary javax.print.DocFlavor.READER conformance: white-box structure + black-box behavior.
 * @run main READERTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class READERTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocFlavor$READER");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(DocFlavor.class.isAssignableFrom(c), "extends DocFlavor");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DocFlavor.READER.TEXT_PLAIN != null, "TEXT_PLAIN non-null");
        check("java.io.Reader".equals(DocFlavor.READER.TEXT_PLAIN.getRepresentationClassName()), "TEXT_PLAIN repr class");
        check(DocFlavor.READER.TEXT_HTML != null, "TEXT_HTML non-null");
        check("java.io.Reader".equals(DocFlavor.READER.TEXT_HTML.getRepresentationClassName()), "TEXT_HTML repr class");
        check("text/plain; charset=\"utf-16\"".equals(DocFlavor.READER.TEXT_PLAIN.getMimeType()), "TEXT_PLAIN mime exact");
        check(DocFlavor.READER.TEXT_PLAIN.equals(DocFlavor.READER.TEXT_PLAIN) && DocFlavor.READER.TEXT_PLAIN.hashCode() == DocFlavor.READER.TEXT_PLAIN.hashCode(), "TEXT_PLAIN equals/hash");
        check(!DocFlavor.READER.TEXT_PLAIN.equals(DocFlavor.READER.TEXT_HTML), "distinct constants unequal");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
