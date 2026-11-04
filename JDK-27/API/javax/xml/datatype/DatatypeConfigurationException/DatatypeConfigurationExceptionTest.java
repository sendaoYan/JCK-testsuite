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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
