/*
 * @test
 * @summary javax.xml.stream.XMLEventReader conformance: API type verification.
 * @run main XMLEventReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventReaderTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLEventReader");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "java.util.Iterator"), "interface java.util.Iterator");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "getElementText"), "method getElementText");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "hasNext"), "method hasNext");
        check(hasMethod(c, "nextEvent"), "method nextEvent");
        check(hasMethod(c, "nextTag"), "method nextTag");
        check(hasMethod(c, "peek"), "method peek");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
