/*
 * @test
 * @summary org.xml.sax.ext.Locator2Impl conformance: white-box structure + black-box behavior.
 * @run main Locator2ImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class Locator2ImplTest {
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

        Class<?> c = Class.forName("org.xml.sax.ext.Locator2Impl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.LocatorImpl"), "superclass org.xml.sax.helpers.LocatorImpl");
        check(hasInterface(c, "org.xml.sax.ext.Locator2"), "interface org.xml.sax.ext.Locator2");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getXMLVersion"), "method getXMLVersion");
        check(hasMethod(c, "setEncoding"), "method setEncoding");
        check(hasMethod(c, "setXMLVersion"), "method setXMLVersion");

        org.xml.sax.ext.Locator2Impl l = new org.xml.sax.ext.Locator2Impl();
        l.setXMLVersion("1.0");
        l.setEncoding("UTF-8");
        check("1.0".equals(l.getXMLVersion()), "xmlVersion");
        check("UTF-8".equals(l.getEncoding()), "encoding");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.ext.Locator2Impl l = new org.xml.sax.ext.Locator2Impl();
        l.setXMLVersion("1.1"); l.setEncoding("UTF-8");
        check("1.1".equals(l.getXMLVersion()) && "UTF-8".equals(l.getEncoding()), "xml version + encoding");
        org.xml.sax.helpers.LocatorImpl base = new org.xml.sax.helpers.LocatorImpl();
        base.setLineNumber(3);
        org.xml.sax.ext.Locator2Impl copy = new org.xml.sax.ext.Locator2Impl(base);
        check(copy.getLineNumber() == 3, "copy ctor inherits locator");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Locator2Impl contract ----
    static void deepContract() throws Exception {
        org.xml.sax.ext.Locator2Impl l = new org.xml.sax.ext.Locator2Impl();
        check(l.getXMLVersion() == null && l.getEncoding() == null, "fresh Locator2Impl: version/encoding null");
        check(l.getLineNumber() == 0 && l.getColumnNumber() == 0, "fresh Locator2Impl: position 0/0 (inherited impl default)");
        l.setXMLVersion("1.1");
        l.setEncoding("UTF-8");
        check("1.1".equals(l.getXMLVersion()) && "UTF-8".equals(l.getEncoding()), "setXMLVersion/setEncoding round-trip");
        l.setPublicId("pub"); l.setSystemId("sys"); l.setLineNumber(4); l.setColumnNumber(6);
        check("pub".equals(l.getPublicId()) && "sys".equals(l.getSystemId()), "inherited id setters round-trip");
        check(l.getLineNumber() == 4 && l.getColumnNumber() == 6, "inherited position setters round-trip");
        // copy constructor snapshots another locator incl. version/encoding
        org.xml.sax.ext.Locator2Impl copy = new org.xml.sax.ext.Locator2Impl(l);
        check(copy.getXMLVersion().equals("1.1") && copy.getEncoding().equals("UTF-8"), "copy constructor carries version/encoding");
        check(copy.getLineNumber() == 4 && copy.getPublicId().equals("pub"), "copy constructor carries position/ids");
        check(org.xml.sax.helpers.LocatorImpl.class.isAssignableFrom(org.xml.sax.ext.Locator2Impl.class), "Locator2Impl extends LocatorImpl");
    }
}
