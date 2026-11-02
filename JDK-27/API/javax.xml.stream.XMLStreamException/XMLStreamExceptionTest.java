/*
 * @test
 * @summary javax.xml.stream.XMLStreamException conformance: API type verification.
 * @run main XMLStreamExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLStreamExceptionTest {
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


    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getNestedException"), "method getNestedException");

        javax.xml.stream.XMLStreamException e = new javax.xml.stream.XMLStreamException("m");
        check("m".equals(e.getMessage()), "msg");
        check(e.getLocation() == null, "location null");
        check(e.getNestedException() == null, "nested null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
