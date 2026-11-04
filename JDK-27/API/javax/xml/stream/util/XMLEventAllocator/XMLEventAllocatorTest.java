/*
 * @test
 * @summary javax.xml.stream.util.XMLEventAllocator conformance: white-box structure + black-box behavior.
 * @run main XMLEventAllocatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventAllocatorTest {
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

        Class<?> c = Class.forName("javax.xml.stream.util.XMLEventAllocator");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "allocate"), "method allocate");
        check(hasMethod(c, "newInstance"), "method newInstance");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.util.XMLEventAllocator a = new javax.xml.stream.util.XMLEventAllocator(){
            public javax.xml.stream.util.XMLEventAllocator newInstance() { return null; }
            public javax.xml.stream.events.XMLEvent allocate(javax.xml.stream.XMLStreamReader p0) { return null; }
            public void allocate(javax.xml.stream.XMLStreamReader p0, javax.xml.stream.util.XMLEventConsumer p1) { }
        };
        check(a != null, "anon impl of XMLEventAllocator");
        check(a.newInstance() == null, "newInstance() returns fixed default");
        check(a.allocate((javax.xml.stream.XMLStreamReader) null) == null, "allocate() returns fixed default");
        a.allocate((javax.xml.stream.XMLStreamReader) null, (javax.xml.stream.util.XMLEventConsumer) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
