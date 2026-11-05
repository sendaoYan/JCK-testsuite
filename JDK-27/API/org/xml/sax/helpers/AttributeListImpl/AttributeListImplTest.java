/*
 * @test
 * @summary org.xml.sax.helpers.AttributeListImpl conformance: white-box structure + black-box behavior.
 * @run main AttributeListImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributeListImplTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.AttributeListImpl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.AttributeList"), "interface org.xml.sax.AttributeList");
        check(hasMethod(c, "addAttribute"), "method addAttribute");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "removeAttribute"), "method removeAttribute");
        check(hasMethod(c, "setAttributeList"), "method setAttributeList");

        org.xml.sax.helpers.AttributeListImpl a = new org.xml.sax.helpers.AttributeListImpl();
        a.addAttribute("name", "CDATA", "value");
        check(a.getLength() == 1, "length");
        check("value".equals(a.getValue("name")), "value");
        check("name".equals(a.getName(0)), "name");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.helpers.AttributeListImpl a = new org.xml.sax.helpers.AttributeListImpl();
        check(a.getLength() == 0, "initially empty");
        a.addAttribute("name", "CDATA", "value");
        check(a.getLength() == 1, "addAttribute");
        check("name".equals(a.getName(0)) && "CDATA".equals(a.getType(0)) && "value".equals(a.getValue(0)), "getters");
        check("value".equals(a.getValue("name")), "getValue(name)");
        check("CDATA".equals(a.getType("name")), "getType(name)");
        a.addAttribute("n2", "ID", "v2");
        a.removeAttribute("name");
        check(a.getLength() == 1 && "n2".equals(a.getName(0)), "removeAttribute");
        a.clear(); check(a.getLength() == 0, "clear");
        org.xml.sax.helpers.AttributeListImpl src = new org.xml.sax.helpers.AttributeListImpl();
        src.addAttribute("k", "CDATA", "v");
        org.xml.sax.helpers.AttributeListImpl copy = new org.xml.sax.helpers.AttributeListImpl(src);
        check(copy.getLength() == 1 && "v".equals(copy.getValue("k")), "copy ctor");
        org.xml.sax.helpers.AttributeListImpl dst = new org.xml.sax.helpers.AttributeListImpl();
        dst.setAttributeList(src);
        check(dst.getLength() == 1, "setAttributeList");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral AttributeListImpl contract (SAX1 list lifecycle) ----
    static void deepContract() throws Exception {
        org.xml.sax.helpers.AttributeListImpl a = new org.xml.sax.helpers.AttributeListImpl();
        check(a.getLength() == 0, "fresh list empty");
        a.addAttribute("x", "CDATA", "1");
        a.addAttribute("y", "NMTOKEN", "n");
        check(a.getLength() == 2, "addAttribute appends");
        check("x".equals(a.getName(0)) && "y".equals(a.getName(1)), "getName by index");
        check("CDATA".equals(a.getType(0)) && "NMTOKEN".equals(a.getType(1)), "getType by index");
        check("1".equals(a.getValue(0)) && "n".equals(a.getValue(1)), "getValue by index");
        check("CDATA".equals(a.getType("x")) && "1".equals(a.getValue("x")), "lookup by name");
        check(a.getType("missing") == null && a.getValue("missing") == null, "missing name -> null type/value");
        check(a.getName(9) == null && a.getType(9) == null && a.getValue(9) == null, "out-of-range getters -> null (no exception)");
        a.removeAttribute("x");
        check(a.getLength() == 1 && "y".equals(a.getName(0)), "removeAttribute shifts remaining entries");
        org.xml.sax.helpers.AttributeListImpl copy = new org.xml.sax.helpers.AttributeListImpl(a);
        check(copy.getLength() == 1 && "n".equals(copy.getValue("y")), "copy constructor snapshots the list");
        a.addAttribute("z", "CDATA", "3");
        check(copy.getValue("z") == null, "copy is independent of later additions");
        a.clear();
        check(a.getLength() == 0, "clear empties the list");
        check(copy.getLength() == 1, "clear does not affect an earlier copy");
    }
}
