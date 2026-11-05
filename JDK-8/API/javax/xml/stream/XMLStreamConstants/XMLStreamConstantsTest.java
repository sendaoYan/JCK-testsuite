/*
 * @test
 * @summary javax.xml.stream.XMLStreamConstants conformance: interface hierarchy and methods.
 * @run main XMLStreamConstantsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLStreamConstantsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamConstants");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
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
