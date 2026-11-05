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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: 4 constructor forms + cause chain ----
    static void testContractDeep() throws Exception {
        javax.print.PrintException e0 = new javax.print.PrintException();
        check(e0.getMessage() == null && e0.getCause() == null, "no-arg null message+cause");
        javax.print.PrintException e1 = new javax.print.PrintException("job failed");
        check("job failed".equals(e1.getMessage()), "message form");
        check(e1.getCause() == null, "no cause");
        java.io.IOException cause = new java.io.IOException("disk");
        javax.print.PrintException e2 = new javax.print.PrintException(cause);
        check(e2.getCause() == cause, "cause identity");
        javax.print.PrintException e3 = new javax.print.PrintException("wrapped", cause);
        check("wrapped".equals(e3.getMessage()) && e3.getCause() == cause, "message+cause");
        check(e3 instanceof java.io.Serializable && e3 instanceof Exception, "Exception/Serializable type");
        Object rt = serRoundTrip(e3);
        check(rt instanceof javax.print.PrintException && "wrapped".equals(((javax.print.PrintException) rt).getMessage()), "serialization round-trip");
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
