/*
 * @test
 * @summary javax.xml.datatype.DatatypeConfigurationException conformance: white-box structure + black-box behavior.
 * @run main DatatypeConfigurationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DatatypeConfigurationExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.datatype.DatatypeConfigurationException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");

        javax.xml.datatype.DatatypeConfigurationException e = new javax.xml.datatype.DatatypeConfigurationException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.datatype.DatatypeConfigurationException e = new javax.xml.datatype.DatatypeConfigurationException("m");
        check("m".equals(e.getMessage()), "message ctor");
        javax.xml.datatype.DatatypeConfigurationException e2 = new javax.xml.datatype.DatatypeConfigurationException("m", new RuntimeException("c"));
        check("m".equals(e2.getMessage()) && e2.getCause() != null, "message + throwable");
        javax.xml.datatype.DatatypeConfigurationException e3 = new javax.xml.datatype.DatatypeConfigurationException();
        check(e3.getMessage() == null, "no-arg");
        check(e instanceof java.lang.Exception, "is Exception");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepChains();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (constructor chain matrix) ----
    static void testDeepChains() throws Exception {
        RuntimeException root = new RuntimeException("root");
        javax.xml.datatype.DatatypeConfigurationException e2 =
            new javax.xml.datatype.DatatypeConfigurationException("boom", root);
        check(e2.getCause() == root, "(String,Throwable) cause identity");
        javax.xml.datatype.DatatypeConfigurationException e3 =
            new javax.xml.datatype.DatatypeConfigurationException(root);
        check(e3.getCause() == root, "(Throwable) cause identity");
        check(e3.getMessage() != null && e3.getMessage().contains("root"),
              "(Throwable) message derived from cause toString");
        check(e3 instanceof java.io.Serializable, "Serializable via Exception");
        java.lang.reflect.Constructor<?>[] ctors =
            Class.forName("javax.xml.datatype.DatatypeConfigurationException").getDeclaredConstructors();
        check(ctors.length == 4, "four constructors");
        int stringOnly = 0, stringThrowable = 0, throwableOnly = 0, noArg = 0;
        for (java.lang.reflect.Constructor<?> ct : ctors) {
            Class<?>[] ps = ct.getParameterTypes();
            if (ps.length == 1 && ps[0] == String.class) stringOnly++;
            else if (ps.length == 2 && ps[0] == String.class && Throwable.class.isAssignableFrom(ps[1])) stringThrowable++;
            else if (ps.length == 1 && Throwable.class.isAssignableFrom(ps[0])) throwableOnly++;
            else if (ps.length == 0) noArg++;
        }
        check(stringOnly == 1, "one (String) ctor");
        check(stringThrowable == 1, "one (String,Throwable) ctor");
        check(throwableOnly == 1, "one (Throwable) ctor");
        check(noArg == 1, "one () ctor");
    }
}
