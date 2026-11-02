/*
 * @test
 * @summary org.xml.sax.ext.Attributes2Impl conformance: API type verification.
 * @run main Attributes2ImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class Attributes2ImplTest {
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
        Class<?> c = Class.forName("org.xml.sax.ext.Attributes2Impl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.AttributesImpl"), "superclass org.xml.sax.helpers.AttributesImpl");
        check(hasInterface(c, "org.xml.sax.ext.Attributes2"), "interface org.xml.sax.ext.Attributes2");
        check(hasMethod(c, "addAttribute"), "method addAttribute");
        check(hasMethod(c, "isDeclared"), "method isDeclared");
        check(hasMethod(c, "isSpecified"), "method isSpecified");
        check(hasMethod(c, "removeAttribute"), "method removeAttribute");
        check(hasMethod(c, "setAttributes"), "method setAttributes");
        check(hasMethod(c, "setDeclared"), "method setDeclared");
        check(hasMethod(c, "setSpecified"), "method setSpecified");

        org.xml.sax.ext.Attributes2Impl a = new org.xml.sax.ext.Attributes2Impl();
        a.addAttribute("uri", "local", "q", "CDATA", "value");
        a.setDeclared(0, true);
        a.setSpecified(0, true);
        check(a.isDeclared(0), "isDeclared");
        check(a.isSpecified(0), "isSpecified");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
