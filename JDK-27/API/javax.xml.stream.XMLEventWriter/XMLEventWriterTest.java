/*
 * @test
 * @summary javax.xml.stream.XMLEventWriter conformance: API type verification.
 * @run main XMLEventWriterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventWriterTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLEventWriter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.util.XMLEventConsumer"), "interface javax.xml.stream.util.XMLEventConsumer");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "getNamespaceContext"), "method getNamespaceContext");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "setDefaultNamespace"), "method setDefaultNamespace");
        check(hasMethod(c, "setNamespaceContext"), "method setNamespaceContext");
        check(hasMethod(c, "setPrefix"), "method setPrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
