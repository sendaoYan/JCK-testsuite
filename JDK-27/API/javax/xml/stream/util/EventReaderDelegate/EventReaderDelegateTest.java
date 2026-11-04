/*
 * @test
 * @summary javax.xml.stream.util.EventReaderDelegate conformance: white-box structure + black-box behavior.
 * @run main EventReaderDelegateTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EventReaderDelegateTest {
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

        Class<?> c = Class.forName("javax.xml.stream.util.EventReaderDelegate");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.stream.XMLEventReader"), "interface javax.xml.stream.XMLEventReader");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "getElementText"), "method getElementText");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "hasNext"), "method hasNext");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "nextEvent"), "method nextEvent");
        check(hasMethod(c, "nextTag"), "method nextTag");
        check(hasMethod(c, "peek"), "method peek");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "setParent"), "method setParent");

        javax.xml.stream.util.EventReaderDelegate d = new javax.xml.stream.util.EventReaderDelegate();
        check(d.getParent() == null, "parent null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLEventReader base = javax.xml.stream.XMLInputFactory.newFactory().createXMLEventReader(new java.io.StringReader("<r/>"));
        javax.xml.stream.util.EventReaderDelegate d = new javax.xml.stream.util.EventReaderDelegate(base);
        check(d.getParent() == base, "parent round-trip");
        check(d.hasNext(), "delegates hasNext");
        d.close();
        d.setParent(null); check(d.getParent() == null, "setParent null");
        javax.xml.stream.util.EventReaderDelegate d2 = new javax.xml.stream.util.EventReaderDelegate();
        check(d2.getParent() == null, "no-arg empty");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
