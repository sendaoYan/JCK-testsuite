/*
 * @test
 * @summary javax.xml.stream.XMLStreamConstants conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(javax.xml.stream.XMLStreamConstants.START_ELEMENT == 1, "START_ELEMENT");
        check(javax.xml.stream.XMLStreamConstants.END_ELEMENT == 2, "END_ELEMENT");
        check(javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION == 3, "PROCESSING_INSTRUCTION");
        check(javax.xml.stream.XMLStreamConstants.CHARACTERS == 4, "CHARACTERS");
        check(javax.xml.stream.XMLStreamConstants.COMMENT == 5, "COMMENT");
        check(javax.xml.stream.XMLStreamConstants.SPACE == 6, "SPACE");
        check(javax.xml.stream.XMLStreamConstants.START_DOCUMENT == 7, "START_DOCUMENT");
        check(javax.xml.stream.XMLStreamConstants.END_DOCUMENT == 8, "END_DOCUMENT");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE == 9, "ENTITY_REFERENCE");
        check(javax.xml.stream.XMLStreamConstants.ATTRIBUTE == 10, "ATTRIBUTE");
        check(javax.xml.stream.XMLStreamConstants.DTD == 11, "DTD");
        check(javax.xml.stream.XMLStreamConstants.CDATA == 12, "CDATA");
        check(javax.xml.stream.XMLStreamConstants.NAMESPACE == 13, "NAMESPACE");
        check(javax.xml.stream.XMLStreamConstants.NOTATION_DECLARATION == 14, "NOTATION_DECLARATION");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_DECLARATION == 15, "ENTITY_DECLARATION");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
