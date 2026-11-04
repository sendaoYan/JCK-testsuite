/*
 * @test
 * @summary javax.print.PrintException conformance: white-box structure + black-box behavior.
 * @run main PrintExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.PrintException");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(Exception.class.isAssignableFrom(c), "extends Exception");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(new PrintException().getMessage() == null, "no-arg");
        check("msg".equals(new PrintException("msg").getMessage()), "String ctor");
        Exception cause = new Exception("cause");
        check(cause == new PrintException(cause).getCause(), "cause ctor");
        check("m".equals(new PrintException("m", cause).getMessage()) && cause == new PrintException("m", cause).getCause(), "String+cause ctor");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
