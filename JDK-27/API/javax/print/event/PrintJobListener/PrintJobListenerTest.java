/*
 * @test
 * @summary javax.print.event.PrintJobListener conformance: white-box structure + black-box behavior.
 * @run main PrintJobListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintJobListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.event.PrintJobListener");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "printDataTransferCompleted"), "declares printDataTransferCompleted");
        check(hasMethod(c, "printJobCompleted"), "declares printJobCompleted");
        check(hasMethod(c, "printJobFailed"), "declares printJobFailed");
        check(hasMethod(c, "printJobCanceled"), "declares printJobCanceled");
        check(hasMethod(c, "printJobNoMoreEvents"), "declares printJobNoMoreEvents");
        check(hasMethod(c, "printJobRequiresAttention"), "declares printJobRequiresAttention");
    }

    static void testBlackBox() throws Exception { /* pure interface */ }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
