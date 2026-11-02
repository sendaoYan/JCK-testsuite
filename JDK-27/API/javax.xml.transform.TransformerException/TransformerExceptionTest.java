/*
 * @test
 * @summary javax.xml.transform.TransformerException conformance: API type verification.
 * @run main TransformerExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformerExceptionTest {
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
        Class<?> c = Class.forName("javax.xml.transform.TransformerException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass java.lang.Exception");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "getException"), "method getException");
        check(hasMethod(c, "getLocationAsString"), "method getLocationAsString");
        check(hasMethod(c, "getLocator"), "method getLocator");
        check(hasMethod(c, "getMessageAndLocation"), "method getMessageAndLocation");
        check(hasMethod(c, "initCause"), "method initCause");
        check(hasMethod(c, "printStackTrace"), "method printStackTrace");
        check(hasMethod(c, "setLocator"), "method setLocator");

        javax.xml.transform.TransformerException e = new javax.xml.transform.TransformerException("m", (javax.xml.transform.SourceLocator) null);
        check("m".equals(e.getMessage()), "msg");
        check(e.getLocator() == null, "locator null");
        check(e.getException() == null, "exception null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
