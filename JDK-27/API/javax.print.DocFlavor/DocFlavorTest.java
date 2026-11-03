/*
 * @test
 * @summary javax.print.DocFlavor conformance: white-box structure + black-box behavior.
 * @run main DocFlavorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DocFlavorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocFlavor");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(java.io.Serializable.class.isAssignableFrom(c) && Cloneable.class.isAssignableFrom(c), "Serializable+Cloneable");
        check(hasMethod(c, "getMimeType") && hasMethod(c, "getRepresentationClassName") && hasMethod(c, "getMediaType") && hasMethod(c, "getMediaSubtype") && hasMethod(c, "getParameter") && hasMethod(c, "toString"), "methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DocFlavor f = new DocFlavor("text/plain; charset=utf-8", "java.io.Reader");
        check("text/plain; charset=\"utf-8\"".equals(f.getMimeType()), "canonical MIME");
        check("text".equals(f.getMediaType()) && "plain".equals(f.getMediaSubtype()), "media type/subtype");
        check("utf-8".equals(f.getParameter("charset")), "getParameter charset");
        check(f.getParameter("absent") == null, "absent parameter null");
        check("java.io.Reader".equals(f.getRepresentationClassName()), "representation class");
        check("text/plain; charset=\"utf-8\"; class=\"java.io.Reader\"".equals(f.toString()), "toString");
        DocFlavor f2 = new DocFlavor("TEXT/PLAIN; charset=UTF-8", "java.io.Reader");
        check(f.equals(f2), "equals canonicalized same");
        check(f.hashCode() == f2.hashCode(), "hashCode consistent");
        check(!f.equals(new DocFlavor("text/plain; charset=us-ascii", "java.io.Reader")), "different charset unequal");
        check(!f.equals(new DocFlavor("text/plain; charset=utf-8", "java.io.InputStream")), "different repr unequal");
        check(!f.equals(null) && !f.equals("x"), "not equals null/foreign");
        boolean threw = false; try { new DocFlavor(null, "java.io.Reader"); } catch (NullPointerException e) { threw = true; }
        check(threw, "null mime NPE");
        boolean threw2 = false; try { new DocFlavor("text/plain", null); } catch (NullPointerException e) { threw2 = true; }
        check(threw2, "null class NPE");
        boolean threw3 = false; try { new DocFlavor("not a mime", "java.io.Reader"); } catch (IllegalArgumentException e) { threw3 = true; }
        check(threw3, "invalid mime IAE");
        check(DocFlavor.hostEncoding != null, "hostEncoding non-null");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
