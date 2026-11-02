/*
 * @test
 * @summary javax.xml.stream.XMLStreamConstants conformance: API type verification.
 * @run main XMLStreamConstantsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLStreamConstantsTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamConstants");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasField(c, "ATTRIBUTE"), "field ATTRIBUTE");
        check(hasField(c, "CDATA"), "field CDATA");
        check(hasField(c, "CHARACTERS"), "field CHARACTERS");
        check(hasField(c, "COMMENT"), "field COMMENT");
        check(hasField(c, "DTD"), "field DTD");
        check(hasField(c, "END_DOCUMENT"), "field END_DOCUMENT");
        check(hasField(c, "END_ELEMENT"), "field END_ELEMENT");
        check(hasField(c, "ENTITY_DECLARATION"), "field ENTITY_DECLARATION");
        check(hasField(c, "ENTITY_REFERENCE"), "field ENTITY_REFERENCE");
        check(hasField(c, "NAMESPACE"), "field NAMESPACE");
        check(hasField(c, "NOTATION_DECLARATION"), "field NOTATION_DECLARATION");
        check(hasField(c, "PROCESSING_INSTRUCTION"), "field PROCESSING_INSTRUCTION");
        check(hasField(c, "SPACE"), "field SPACE");
        check(hasField(c, "START_DOCUMENT"), "field START_DOCUMENT");
        check(hasField(c, "START_ELEMENT"), "field START_ELEMENT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
