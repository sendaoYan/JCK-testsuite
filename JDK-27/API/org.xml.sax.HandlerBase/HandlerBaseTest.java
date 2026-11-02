/*
 * @test
 * @summary org.xml.sax.HandlerBase conformance: API type verification.
 * @run main HandlerBaseTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class HandlerBaseTest {
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
        Class<?> c = Class.forName("org.xml.sax.HandlerBase");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "interface org.xml.sax.DocumentHandler");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "interface org.xml.sax.ErrorHandler");
        check(hasMethod(c, "characters"), "method characters");
        check(hasMethod(c, "endDocument"), "method endDocument");
        check(hasMethod(c, "endElement"), "method endElement");
        check(hasMethod(c, "error"), "method error");
        check(hasMethod(c, "fatalError"), "method fatalError");
        check(hasMethod(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasMethod(c, "notationDecl"), "method notationDecl");
        check(hasMethod(c, "processingInstruction"), "method processingInstruction");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "setDocumentLocator"), "method setDocumentLocator");
        check(hasMethod(c, "startDocument"), "method startDocument");
        check(hasMethod(c, "startElement"), "method startElement");
        check(hasMethod(c, "unparsedEntityDecl"), "method unparsedEntityDecl");
        check(hasMethod(c, "warning"), "method warning");

        org.xml.sax.HandlerBase h = new org.xml.sax.HandlerBase();
        h.startDocument();
        h.endDocument();
        check(true, "no-throw");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
