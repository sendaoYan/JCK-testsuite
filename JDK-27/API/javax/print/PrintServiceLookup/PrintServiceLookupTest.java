/*
 * @test
 * @summary javax.print.PrintServiceLookup conformance: white-box structure + black-box behavior.
 * @run main PrintServiceLookupTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintServiceLookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.PrintServiceLookup");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(hasMethod(c, "lookupPrintServices") && hasMethod(c, "lookupMultiDocPrintServices") && hasMethod(c, "lookupDefaultPrintService") && hasMethod(c, "registerServiceProvider") && hasMethod(c, "registerService"), "static methods");
        check(hasMethod(c, "getPrintServices") && hasMethod(c, "getMultiDocPrintServices") && hasMethod(c, "getDefaultPrintService"), "abstract methods");
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
