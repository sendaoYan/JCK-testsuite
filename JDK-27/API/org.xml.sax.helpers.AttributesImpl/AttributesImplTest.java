/*
 * @test
 * @summary org.xml.sax.helpers.AttributesImpl conformance: API type verification.
 * @run main AttributesImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributesImplTest {
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
        Class<?> c = Class.forName("org.xml.sax.helpers.AttributesImpl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.Attributes"), "interface org.xml.sax.Attributes");
        check(hasMethod(c, "addAttribute"), "method addAttribute");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "getIndex"), "method getIndex");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getLocalName"), "method getLocalName");
        check(hasMethod(c, "getQName"), "method getQName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getURI"), "method getURI");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "removeAttribute"), "method removeAttribute");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        check(hasMethod(c, "setAttributes"), "method setAttributes");
        check(hasMethod(c, "setLocalName"), "method setLocalName");
        check(hasMethod(c, "setQName"), "method setQName");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "setURI"), "method setURI");
        check(hasMethod(c, "setValue"), "method setValue");

        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        a.addAttribute("uri", "local", "q", "CDATA", "value");
        check(a.getLength() == 1, "length");
        check("value".equals(a.getValue(0)), "value");
        check("local".equals(a.getLocalName(0)), "localName");
        check("q".equals(a.getQName(0)), "qName");
        check("uri".equals(a.getURI(0)), "uri");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
