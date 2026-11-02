/*
 * @test
 * @summary javax.xml.transform.OutputKeys conformance: API type verification.
 * @run main OutputKeysTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class OutputKeysTest {
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
        Class<?> c = Class.forName("javax.xml.transform.OutputKeys");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasField(c, "CDATA_SECTION_ELEMENTS"), "field CDATA_SECTION_ELEMENTS");
        check(hasField(c, "DOCTYPE_PUBLIC"), "field DOCTYPE_PUBLIC");
        check(hasField(c, "DOCTYPE_SYSTEM"), "field DOCTYPE_SYSTEM");
        check(hasField(c, "ENCODING"), "field ENCODING");
        check(hasField(c, "INDENT"), "field INDENT");
        check(hasField(c, "MEDIA_TYPE"), "field MEDIA_TYPE");
        check(hasField(c, "METHOD"), "field METHOD");
        check(hasField(c, "OMIT_XML_DECLARATION"), "field OMIT_XML_DECLARATION");
        check(hasField(c, "STANDALONE"), "field STANDALONE");
        check(hasField(c, "VERSION"), "field VERSION");

        check(javax.xml.transform.OutputKeys.METHOD != null, "METHOD");
        check(javax.xml.transform.OutputKeys.VERSION != null, "VERSION");
        check(javax.xml.transform.OutputKeys.ENCODING != null, "ENCODING");
        check(javax.xml.transform.OutputKeys.INDENT != null, "INDENT");
        check(javax.xml.transform.OutputKeys.MEDIA_TYPE != null, "MEDIA_TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
