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
        testConstantValues();
        testFieldSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened: exact event-code contract (all 15 constants) ----
    static void testConstantValues() {
        check(javax.xml.stream.XMLStreamConstants.START_ELEMENT == 1, "START_ELEMENT == 1");
        check(javax.xml.stream.XMLStreamConstants.END_ELEMENT == 2, "END_ELEMENT == 2");
        check(javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION == 3, "PROCESSING_INSTRUCTION == 3");
        check(javax.xml.stream.XMLStreamConstants.CHARACTERS == 4, "CHARACTERS == 4");
        check(javax.xml.stream.XMLStreamConstants.COMMENT == 5, "COMMENT == 5");
        check(javax.xml.stream.XMLStreamConstants.SPACE == 6, "SPACE == 6");
        check(javax.xml.stream.XMLStreamConstants.START_DOCUMENT == 7, "START_DOCUMENT == 7");
        check(javax.xml.stream.XMLStreamConstants.END_DOCUMENT == 8, "END_DOCUMENT == 8");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE == 9, "ENTITY_REFERENCE == 9");
        check(javax.xml.stream.XMLStreamConstants.ATTRIBUTE == 10, "ATTRIBUTE == 10");
        check(javax.xml.stream.XMLStreamConstants.DTD == 11, "DTD == 11");
        check(javax.xml.stream.XMLStreamConstants.CDATA == 12, "CDATA == 12");
        check(javax.xml.stream.XMLStreamConstants.NAMESPACE == 13, "NAMESPACE == 13");
        check(javax.xml.stream.XMLStreamConstants.NOTATION_DECLARATION == 14, "NOTATION_DECLARATION == 14");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_DECLARATION == 15, "ENTITY_DECLARATION == 15");
        // the 11 document-event codes form a dense 1..11 range; extra codes 12..15 are event-object only
        check(javax.xml.stream.XMLStreamConstants.CDATA != javax.xml.stream.XMLStreamConstants.CHARACTERS,
            "CDATA distinct from CHARACTERS despite stream reporting CHARACTERS for CDATA content");
    }

    // ---- Deepened: white-box field surface (public static final int, all 15) ----
    static void testFieldSurface() throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamConstants");
        String[] names = {"START_ELEMENT", "END_ELEMENT", "PROCESSING_INSTRUCTION", "CHARACTERS",
            "COMMENT", "SPACE", "START_DOCUMENT", "END_DOCUMENT", "ENTITY_REFERENCE",
            "ATTRIBUTE", "DTD", "CDATA", "NAMESPACE", "NOTATION_DECLARATION", "ENTITY_DECLARATION"};
        java.util.Set<String> fields = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getFields()) fields.add(f.getName());
        int expected = 0;
        for (String n : names) {
            check(fields.contains(n), "field " + n);
            check(c.getField(n).getType() == int.class, "field " + n + " is int");
            check(java.lang.reflect.Modifier.isFinal(c.getField(n).getModifiers())
                && java.lang.reflect.Modifier.isStatic(c.getField(n).getModifiers()), "field " + n + " static final");
            expected++;
        }
        check(fields.size() == expected, "exactly " + expected + " public fields, got " + fields.size());
    }
}
