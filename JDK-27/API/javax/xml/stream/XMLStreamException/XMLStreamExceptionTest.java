/*
 * @test
 * @summary javax.xml.stream.XMLStreamException conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLStreamException e = new javax.xml.stream.XMLStreamException("m");
        check("m".equals(e.getMessage()) && e.getNestedException() == null, "message ctor");
        java.lang.Throwable c = new RuntimeException("r");
        javax.xml.stream.XMLStreamException e2 = new javax.xml.stream.XMLStreamException("m", c);
        check(e2.getNestedException() == c && e2.getCause() == c, "message + throwable");
        javax.xml.stream.XMLStreamException e3 = new javax.xml.stream.XMLStreamException(c);
        check(e3.getNestedException() == c && e3.getLocation() == null, "throwable ctor");
        javax.xml.stream.XMLStreamException e4 = new javax.xml.stream.XMLStreamException();
        check(e4.getMessage() == null, "no-arg");
        check(e instanceof java.lang.Exception, "is Exception");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorChain();
        testLocationDecoration();
        testFromParser();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: all constructor overloads ----
    static void testConstructorChain() {
        javax.xml.stream.Location loc = fixedLocation();
        javax.xml.stream.XMLStreamException e1 = new javax.xml.stream.XMLStreamException("m1", loc);
        check(e1.getMessage() != null && e1.getMessage().contains("m1"), "(String,Location) message decorated with location");
        check(e1.getLocation() == loc, "(String,Location) location identity");
        check(e1.getNestedException() == null, "(String,Location) no nested");
        java.lang.RuntimeException cause = new java.lang.RuntimeException("why");
        javax.xml.stream.XMLStreamException e2 = new javax.xml.stream.XMLStreamException("m2", loc, cause);
        check(e2.getLocation() == loc, "(String,Location,Throwable) location identity");
        check(e2.getNestedException() == cause, "(String,Location,Throwable) nested exception");
        // JDK-8 quirk: this overload does NOT wire getCause() (JDK-27 does) - locked per release
        check(e2.getCause() == cause, "JDK-27: (String,Location,Throwable) wires getCause() (JDK-8 leaves it null)");
        javax.xml.stream.XMLStreamException e3 = new javax.xml.stream.XMLStreamException("m3", cause);
        check(e3.getNestedException() == cause && e3.getLocation() == null, "(String,Throwable) nested set, no location");
        check("m3".equals(e3.getMessage()), "(String,Throwable) message kept");
        javax.xml.stream.XMLStreamException e4 = new javax.xml.stream.XMLStreamException(cause);
        check(e4.getNestedException() == cause, "(Throwable) nested set");
        check(e4.getMessage() != null && e4.getMessage().contains("why"), "(Throwable) message derived from cause toString");
        javax.xml.stream.XMLStreamException e5 = new javax.xml.stream.XMLStreamException();
        check(e5.getMessage() == null && e5.getLocation() == null && e5.getNestedException() == null,
            "no-arg ctor all null");
        check(e5 instanceof java.lang.Exception, "is Exception");
        // Throwable chaining through initCause still possible
        javax.xml.stream.XMLStreamException e6 = new javax.xml.stream.XMLStreamException("m6");
        e6.initCause(cause);
        check(e6.getCause() == cause, "initCause works when not set in ctor");
    }

    // ---- Deepened 2: message decoration with Location ----
    static void testLocationDecoration() {
        javax.xml.stream.Location loc = fixedLocation();
        javax.xml.stream.XMLStreamException e = new javax.xml.stream.XMLStreamException("boom", loc);
        String m = e.getMessage();
        check(m.contains("boom") && m.contains("4") && m.contains("5"),
            "message decorated with row,col from Location, got [" + m.replace("\n", "\\n") + "]");
        check(m.startsWith("ParseError"), "decoration prefix ParseError");
        // Location values surface through getLocation
        check(e.getLocation().getLineNumber() == 4 && e.getLocation().getColumnNumber() == 5
            && e.getLocation().getCharacterOffset() == 6 && "P".equals(e.getLocation().getPublicId())
            && "S".equals(e.getLocation().getSystemId()), "location getters");
    }

    // ---- Deepened 3: exceptions raised by the real parser ----
    static void testFromParser() throws Exception {
        try {
            javax.xml.stream.XMLStreamReader r = javax.xml.stream.XMLInputFactory.newFactory()
                .createXMLStreamReader(new java.io.StringReader("<a><b></a>"));
            while (r.hasNext()) r.next();
            check(false, "malformed doc must throw");
        } catch (javax.xml.stream.XMLStreamException e) {
            check(e.getLocation() != null && e.getLocation().getLineNumber() == 1,
                "parser-raised exception carries line 1 location");
            check(e.getMessage() != null && e.getMessage().length() > 0, "parser-raised exception has message");
        }
    }

    static javax.xml.stream.Location fixedLocation() {
        return new javax.xml.stream.Location() {
            public int getLineNumber() { return 4; }
            public int getColumnNumber() { return 5; }
            public int getCharacterOffset() { return 6; }
            public String getPublicId() { return "P"; }
            public String getSystemId() { return "S"; }
            public String toString() { return "Fixed"; }
        };
    }
}
