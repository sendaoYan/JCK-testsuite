/*
 * @test
 * @summary org.xml.sax.InputSource conformance: API type verification.
 * @run main InputSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class InputSourceTest {
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
        Class<?> c = Class.forName("org.xml.sax.InputSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getByteStream"), "method getByteStream");
        check(hasMethod(c, "getCharacterStream"), "method getCharacterStream");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setByteStream"), "method setByteStream");
        check(hasMethod(c, "setCharacterStream"), "method setCharacterStream");
        check(hasMethod(c, "setEncoding"), "method setEncoding");
        check(hasMethod(c, "setPublicId"), "method setPublicId");
        check(hasMethod(c, "setSystemId"), "method setSystemId");

        org.xml.sax.InputSource s = new org.xml.sax.InputSource();
        s.setPublicId("p");
        s.setSystemId("s");
        check("p".equals(s.getPublicId()), "publicId");
        check("s".equals(s.getSystemId()), "systemId");
        check(!s.isEmpty(), "not empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
