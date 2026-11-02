/*
 * @test
 * @summary javax.xml.transform.stax.StAXResult conformance: API type verification.
 * @run main StAXResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StAXResultTest {
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
        Class<?> c = Class.forName("javax.xml.transform.stax.StAXResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Result"), "interface javax.xml.transform.Result");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getXMLEventWriter"), "method getXMLEventWriter");
        check(hasMethod(c, "getXMLStreamWriter"), "method getXMLStreamWriter");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.stream.XMLStreamWriter w = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r = new javax.xml.transform.stax.StAXResult(w);
        check(r.getXMLStreamWriter() == w, "getXMLStreamWriter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
