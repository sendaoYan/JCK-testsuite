/*
 * @test
 * @summary org.xml.sax.helpers.AttributeListImpl conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
