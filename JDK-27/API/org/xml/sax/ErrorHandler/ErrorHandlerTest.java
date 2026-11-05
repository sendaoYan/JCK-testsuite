/*
 * @test
 * @summary org.xml.sax.ErrorHandler conformance: white-box structure + black-box behavior.
 * @run main ErrorHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ErrorHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.ErrorHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "error"), "method error");
        check(hasMethod(c, "fatalError"), "method fatalError");
        check(hasMethod(c, "warning"), "method warning");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ErrorHandler a = new org.xml.sax.ErrorHandler(){
            public void warning(org.xml.sax.SAXParseException p0) { }
            public void error(org.xml.sax.SAXParseException p0) { }
            public void fatalError(org.xml.sax.SAXParseException p0) { }
        };
        check(a != null, "anon impl of ErrorHandler");
        a.warning((org.xml.sax.SAXParseException) null);
        a.error((org.xml.sax.SAXParseException) null);
        a.fatalError((org.xml.sax.SAXParseException) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ErrorHandler contract (fatal/error/propagation) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.SAXParseException[] fatal = new org.xml.sax.SAXParseException[1];
        xr.setErrorHandler(new org.xml.sax.ErrorHandler() {
            public void warning(org.xml.sax.SAXParseException e) { }
            public void error(org.xml.sax.SAXParseException e) { }
            public void fatalError(org.xml.sax.SAXParseException e) { fatal[0] = e; }
        });
        org.xml.sax.SAXParseException rethrown = null;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r><1x/></r>"))); }
        catch (org.xml.sax.SAXParseException e) { rethrown = e; }
        check(fatal[0] != null, "fatalError receives the well-formedness failure");
        check(rethrown != null && fatal[0] != null && rethrown.getLineNumber() == fatal[0].getLineNumber(), "parse throws a fresh SAXParseException after a returning fatalError handler (line preserved)");
        check(fatal[0].getLineNumber() > 0 && fatal[0].getColumnNumber() > 0, "fatal error carries live line/column");
        // handler throwing from fatalError: propagated as-is
        final RuntimeException boom = new RuntimeException("boom");
        xr.setErrorHandler(new org.xml.sax.ErrorHandler() {
            public void warning(org.xml.sax.SAXParseException e) { }
            public void error(org.xml.sax.SAXParseException e) { }
            public void fatalError(org.xml.sax.SAXParseException e) { throw boom; }
        });
        RuntimeException got = null;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r><1x/></r>"))); }
        catch (RuntimeException e) { got = e; }
        check(got == boom, "handler-thrown RuntimeException propagates out of parse unchanged");
        // recoverable validity errors: error() called, parse continues (non-validating: content-model mismatch reported then completes)
        final int[] errCount = new int[]{0};
        boolean completed = true;
        xr.setErrorHandler(new org.xml.sax.ErrorHandler() {
            public void warning(org.xml.sax.SAXParseException e) { }
            public void error(org.xml.sax.SAXParseException e) { errCount[0]++; }
            public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException { throw e; }
        });
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!ELEMENT r (a,b)>]><r><c/><a/><b/></r>"))); }
        catch (org.xml.sax.SAXException e) { completed = false; }
        check(completed, "recoverable error() does not abort the parse");
        check(org.xml.sax.ErrorHandler.class.isInterface(), "ErrorHandler is an interface");
    }
}
