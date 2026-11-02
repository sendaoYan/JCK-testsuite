/*
 * @test
 * @summary org.xml.sax.Attributes conformance: API type verification.
 * @run main AttributesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributesTest {
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
        Class<?> c = Class.forName("org.xml.sax.Attributes");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getIndex"), "method getIndex");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getLocalName"), "method getLocalName");
        check(hasMethod(c, "getQName"), "method getQName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getURI"), "method getURI");
        check(hasMethod(c, "getValue"), "method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
