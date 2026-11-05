/*
 * @test
 * @summary javax.xml.xpath.XPathException conformance: white-box structure + black-box behavior.
 * @run main XPathExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathExceptionTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.xpath.XPathException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "printStackTrace"), "method printStackTrace");

        javax.xml.xpath.XPathException e = new javax.xml.xpath.XPathException("m");
        check("m".equals(e.getMessage()), "msg");
        check(e.getCause() == null, "cause null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.xpath.XPathException e = new javax.xml.xpath.XPathException("x");
        check("x".equals(e.getMessage()), "message ctor");
        java.lang.Throwable cause = new java.io.IOException("io");
        javax.xml.xpath.XPathException e2 = new javax.xml.xpath.XPathException(cause);
        check(e2.getCause() == cause && "java.io.IOException: io".equals(e2.getMessage()), "throwable ctor");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructors();        System.out.println("PASS (" + passed + " assertions)");
    }


    static void testConstructors() throws Exception {
        // (String): message round-trip, no cause
        javax.xml.xpath.XPathException e1 = new javax.xml.xpath.XPathException("expr failed");
        check("expr failed".equals(e1.getMessage()), "(String) message round-trip");
        check(e1.getCause() == null, "(String) cause null");

        // (String) with null -> NPE (locked actual: "message can't be null")
        try {
            new javax.xml.xpath.XPathException((String) null);
            check(false, "(String null) must throw NPE");
        } catch (NullPointerException npe) {
            check(npe.getMessage() != null && npe.getMessage().contains("message can't be null"),
                  "(String null) -> NPE with locked message");
        }

        // (Throwable): cause round-trip, message derived from cause
        RuntimeException cause = new RuntimeException("root");
        javax.xml.xpath.XPathException e2 = new javax.xml.xpath.XPathException(cause);
        check(e2.getCause() == cause, "(Throwable) cause round-trip");
        check(e2.getMessage() != null && e2.getMessage().contains("root"),
              "(Throwable) message derived from cause");
        // repeated getCause is stable (initCause-like behavior overridden)
        check(e2.getCause() == cause, "getCause stable across calls");

        // (Throwable) null -> NPE (locked actual: "cause can't be null")
        try {
            new javax.xml.xpath.XPathException((Throwable) null);
            check(false, "(Throwable null) must throw NPE");
        } catch (NullPointerException npe) {
            check(npe.getMessage() != null && npe.getMessage().contains("cause can't be null"),
                  "(Throwable null) -> NPE with locked message");
        }

        // serialization round-trip (Exception is Serializable)
        javax.xml.xpath.XPathException e4 = new javax.xml.xpath.XPathException(cause);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e4);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray()));
        javax.xml.xpath.XPathException back = (javax.xml.xpath.XPathException) ois.readObject();
        check(back.getMessage() != null && back.getMessage().contains("root"),
              "serialization round-trip message preserved");
        check(back.getCause() instanceof RuntimeException && "root".equals(back.getCause().getMessage()),
              "serialization round-trip cause");

        // exception family: base type catches all subclasses
        check(new javax.xml.xpath.XPathExpressionException("m")
                  instanceof javax.xml.xpath.XPathException, "XPathExpressionException is-a XPathException");
        check(new javax.xml.xpath.XPathFactoryConfigurationException("m")
                  instanceof javax.xml.xpath.XPathException, "XPathFactoryConfigurationException is-a XPathException");
        check(new javax.xml.xpath.XPathFunctionException("m")
                  instanceof javax.xml.xpath.XPathException, "XPathFunctionException is-a XPathException");
    }
}

