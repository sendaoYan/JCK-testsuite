/*
 * @test
 * @summary org.xml.sax.Parser conformance: white-box structure + black-box behavior.
 * @run main ParserTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ParserTest {
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

        Class<?> c = Class.forName("org.xml.sax.Parser");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "setDTDHandler"), "method setDTDHandler");
        check(hasMethod(c, "setDocumentHandler"), "method setDocumentHandler");
        check(hasMethod(c, "setEntityResolver"), "method setEntityResolver");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setLocale"), "method setLocale");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.Parser a = new org.xml.sax.Parser(){
            public void setLocale(java.util.Locale p0) { }
            public void setEntityResolver(org.xml.sax.EntityResolver p0) { }
            public void setDTDHandler(org.xml.sax.DTDHandler p0) { }
            public void setDocumentHandler(org.xml.sax.DocumentHandler p0) { }
            public void setErrorHandler(org.xml.sax.ErrorHandler p0) { }
            public void parse(org.xml.sax.InputSource p0) { }
            public void parse(java.lang.String p0) { }
        };
        check(a != null, "anon impl of Parser");
        a.setLocale((java.util.Locale) null);
        a.setEntityResolver((org.xml.sax.EntityResolver) null);
        a.setDTDHandler((org.xml.sax.DTDHandler) null);
        a.setDocumentHandler((org.xml.sax.DocumentHandler) null);
        a.setErrorHandler((org.xml.sax.ErrorHandler) null);
        a.parse((org.xml.sax.InputSource) null);
        a.parse("s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
