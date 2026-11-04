/*
 * @test
 * @summary javax.xml.XMLConstants conformance: white-box structure + black-box behavior.
 * @run main XMLConstantsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLConstantsTest {
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

        Class<?> c = Class.forName("javax.xml.XMLConstants");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasField(c, "ACCESS_EXTERNAL_DTD"), "field ACCESS_EXTERNAL_DTD");
        check(hasField(c, "ACCESS_EXTERNAL_SCHEMA"), "field ACCESS_EXTERNAL_SCHEMA");
        check(hasField(c, "ACCESS_EXTERNAL_STYLESHEET"), "field ACCESS_EXTERNAL_STYLESHEET");
        check(hasField(c, "DEFAULT_NS_PREFIX"), "field DEFAULT_NS_PREFIX");
        check(hasField(c, "FEATURE_SECURE_PROCESSING"), "field FEATURE_SECURE_PROCESSING");
        check(hasField(c, "NULL_NS_URI"), "field NULL_NS_URI");
        check(hasField(c, "RELAXNG_NS_URI"), "field RELAXNG_NS_URI");
        check(hasField(c, "USE_CATALOG"), "field USE_CATALOG");
        check(hasField(c, "W3C_XML_SCHEMA_INSTANCE_NS_URI"), "field W3C_XML_SCHEMA_INSTANCE_NS_URI");
        check(hasField(c, "W3C_XML_SCHEMA_NS_URI"), "field W3C_XML_SCHEMA_NS_URI");
        check(hasField(c, "W3C_XPATH_DATATYPE_NS_URI"), "field W3C_XPATH_DATATYPE_NS_URI");
        check(hasField(c, "XMLNS_ATTRIBUTE"), "field XMLNS_ATTRIBUTE");
        check(hasField(c, "XMLNS_ATTRIBUTE_NS_URI"), "field XMLNS_ATTRIBUTE_NS_URI");
        check(hasField(c, "XML_DTD_NS_URI"), "field XML_DTD_NS_URI");
        check(hasField(c, "XML_NS_PREFIX"), "field XML_NS_PREFIX");
        check(hasField(c, "XML_NS_URI"), "field XML_NS_URI");

        check("".equals(javax.xml.XMLConstants.NULL_NS_URI), "NULL_NS_URI");
        check("".equals(javax.xml.XMLConstants.DEFAULT_NS_PREFIX), "DEFAULT_NS_PREFIX");
        check("http://www.w3.org/XML/1998/namespace".equals(javax.xml.XMLConstants.XML_NS_URI), "XML_NS_URI");
        check("xml".equals(javax.xml.XMLConstants.XML_NS_PREFIX), "XML_NS_PREFIX");
        check("http://www.w3.org/2000/xmlns/".equals(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI), "XMLNS_ATTRIBUTE_NS_URI");
        check("xmlns".equals(javax.xml.XMLConstants.XMLNS_ATTRIBUTE), "XMLNS_ATTRIBUTE");
        check("http://www.w3.org/2001/XMLSchema".equals(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI), "W3C_XML_SCHEMA_NS_URI");
        check("http://www.w3.org/2001/XMLSchema-instance".equals(javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), "W3C_XML_SCHEMA_INSTANCE_NS_URI");
        check(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING != null, "FEATURE_SECURE_PROCESSING");
        check(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD != null, "ACCESS_EXTERNAL_DTD");
        check(javax.xml.XMLConstants.USE_CATALOG != null, "USE_CATALOG");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check("".equals(javax.xml.XMLConstants.NULL_NS_URI), "NULL_NS_URI");
        check("http://www.w3.org/XML/1998/namespace".equals(javax.xml.XMLConstants.XML_NS_URI), "XML_NS_URI");
        check("xml".equals(javax.xml.XMLConstants.XML_NS_PREFIX), "XML_NS_PREFIX");
        check("http://www.w3.org/2000/xmlns/".equals(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI), "XMLNS_ATTRIBUTE_NS_URI");
        check("xmlns".equals(javax.xml.XMLConstants.XMLNS_ATTRIBUTE), "XMLNS_ATTRIBUTE");
        check("http://www.w3.org/2001/XMLSchema".equals(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI), "W3C_XML_SCHEMA_NS_URI");
        check("http://www.w3.org/2001/XMLSchema-instance".equals(javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), "W3C_XML_SCHEMA_INSTANCE_NS_URI");
        check("http://www.w3.org/TR/REC-xml".equals(javax.xml.XMLConstants.XML_DTD_NS_URI), "XML_DTD_NS_URI");
        check("http://relaxng.org/ns/structure/1.0".equals(javax.xml.XMLConstants.RELAXNG_NS_URI), "RELAXNG_NS_URI");
        check("http://javax.xml.XMLConstants/feature/secure-processing".equals(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING), "FEATURE_SECURE_PROCESSING");
        check("http://javax.xml.XMLConstants/property/accessExternalDTD".equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD), "ACCESS_EXTERNAL_DTD");
        check("http://javax.xml.XMLConstants/property/accessExternalSchema".equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA), "ACCESS_EXTERNAL_SCHEMA");
        check("http://javax.xml.XMLConstants/property/accessExternalStylesheet".equals(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET), "ACCESS_EXTERNAL_STYLESHEET");
        check("http://javax.xml.XMLConstants/feature/useCatalog".equals(javax.xml.XMLConstants.USE_CATALOG), "USE_CATALOG");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
