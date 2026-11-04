/*
 * @test
 * @summary javax.xml.validation.TypeInfoProvider conformance: white-box structure + black-box behavior.
 * @run main TypeInfoProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TypeInfoProviderTest {
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

        Class<?> c = Class.forName("javax.xml.validation.TypeInfoProvider");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getAttributeTypeInfo"), "method getAttributeTypeInfo");
        check(hasMethod(c, "getElementTypeInfo"), "method getElementTypeInfo");
        check(hasMethod(c, "isIdAttribute"), "method isIdAttribute");
        check(hasMethod(c, "isSpecified"), "method isSpecified");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.validation.Schema s = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
            .newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='n' type='xs:string'/></xs:schema>")));
        javax.xml.validation.TypeInfoProvider tip = s.newValidatorHandler().getTypeInfoProvider();
        check(tip != null, "provider obtained");
        // outside a startElement callback, queries are illegal
        boolean ok = false;
        try { tip.getElementTypeInfo(); }
        catch (IllegalStateException e) { ok = true; }
        check(ok, "getElementTypeInfo outside callback -> IllegalStateException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
