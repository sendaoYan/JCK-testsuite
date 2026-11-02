/*
 * @test
 * @summary org.xml.sax.helpers.LocatorImpl conformance: API type verification.
 * @run main LocatorImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class LocatorImplTest {
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
        Class<?> c = Class.forName("org.xml.sax.helpers.LocatorImpl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.Locator"), "interface org.xml.sax.Locator");
        check(hasMethod(c, "getColumnNumber"), "method getColumnNumber");
        check(hasMethod(c, "getLineNumber"), "method getLineNumber");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "setColumnNumber"), "method setColumnNumber");
        check(hasMethod(c, "setLineNumber"), "method setLineNumber");
        check(hasMethod(c, "setPublicId"), "method setPublicId");
        check(hasMethod(c, "setSystemId"), "method setSystemId");

        org.xml.sax.helpers.LocatorImpl l = new org.xml.sax.helpers.LocatorImpl();
        l.setPublicId("p");
        l.setSystemId("s");
        l.setLineNumber(3);
        l.setColumnNumber(4);
        check("p".equals(l.getPublicId()), "publicId");
        check("s".equals(l.getSystemId()), "systemId");
        check(l.getLineNumber() == 3, "line");
        check(l.getColumnNumber() == 4, "column");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
