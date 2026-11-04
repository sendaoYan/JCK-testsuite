/*
 * @test
 * @summary javax.print.StreamPrintServiceFactory conformance: white-box structure + black-box behavior.
 * @run main StreamPrintServiceFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class StreamPrintServiceFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.StreamPrintServiceFactory");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(hasMethod(c, "lookupStreamPrintServiceFactories") && hasMethod(c, "getOutputFormat") && hasMethod(c, "getSupportedDocFlavors") && hasMethod(c, "getPrintService"), "methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StreamPrintServiceFactory[] fs = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(DocFlavor.SERVICE_FORMATTED.PAGEABLE, "application/postscript");
        check(fs != null, "lookup returns non-null array");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
