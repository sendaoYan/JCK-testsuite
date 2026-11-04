/*
 * @test
 * @summary javax.print.Doc conformance: white-box structure + black-box behavior.
 * @run main DocTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DocTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.Doc");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "getDocFlavor"), "declares getDocFlavor");
        check(hasMethod(c, "getPrintData"), "declares getPrintData");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getReaderForText"), "declares getReaderForText");
        check(hasMethod(c, "getStreamForBytes"), "declares getStreamForBytes");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SimpleDoc d = new SimpleDoc("x", DocFlavor.STRING.TEXT_PLAIN, null);
        check(DocFlavor.STRING.TEXT_PLAIN.equals(d.getDocFlavor()) && "x".equals(d.getPrintData()), "Doc contract");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
