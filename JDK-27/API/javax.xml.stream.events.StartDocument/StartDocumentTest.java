/*
 * @test
 * @summary javax.xml.stream.events.StartDocument conformance: API type verification.
 * @run main StartDocumentTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StartDocumentTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.StartDocument");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "encodingSet"), "method encodingSet");
        check(hasMethod(c, "getCharacterEncodingScheme"), "method getCharacterEncodingScheme");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getVersion"), "method getVersion");
        check(hasMethod(c, "isStandalone"), "method isStandalone");
        check(hasMethod(c, "standaloneSet"), "method standaloneSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
