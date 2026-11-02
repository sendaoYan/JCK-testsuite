/*
 * @test
 * @summary org.xml.sax.ext.DefaultHandler2 conformance: API type verification.
 * @run main DefaultHandler2Test
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DefaultHandler2Test {
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
        Class<?> c = Class.forName("org.xml.sax.ext.DefaultHandler2");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass org.xml.sax.helpers.DefaultHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "interface org.xml.sax.ext.LexicalHandler");
        check(hasInterface(c, "org.xml.sax.ext.DeclHandler"), "interface org.xml.sax.ext.DeclHandler");
        check(hasInterface(c, "org.xml.sax.ext.EntityResolver2"), "interface org.xml.sax.ext.EntityResolver2");
        check(hasMethod(c, "attributeDecl"), "method attributeDecl");
        check(hasMethod(c, "comment"), "method comment");
        check(hasMethod(c, "elementDecl"), "method elementDecl");
        check(hasMethod(c, "endCDATA"), "method endCDATA");
        check(hasMethod(c, "endDTD"), "method endDTD");
        check(hasMethod(c, "endEntity"), "method endEntity");
        check(hasMethod(c, "externalEntityDecl"), "method externalEntityDecl");
        check(hasMethod(c, "getExternalSubset"), "method getExternalSubset");
        check(hasMethod(c, "internalEntityDecl"), "method internalEntityDecl");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "startCDATA"), "method startCDATA");
        check(hasMethod(c, "startDTD"), "method startDTD");
        check(hasMethod(c, "startEntity"), "method startEntity");

        org.xml.sax.ext.DefaultHandler2 h = new org.xml.sax.ext.DefaultHandler2();
        h.startCDATA();
        h.endCDATA();
        h.startDTD("a", "b", "c");
        h.endDTD();
        h.comment(new char[]{'-', '-'}, 0, 2);
        check(true, "no-throw");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
