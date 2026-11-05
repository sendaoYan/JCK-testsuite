/*
 * @test
 * @summary org.xml.sax.DTDHandler conformance: white-box structure + black-box behavior.
 * @run main DTDHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DTDHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.DTDHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "notationDecl"), "method notationDecl");
        check(hasMethod(c, "unparsedEntityDecl"), "method unparsedEntityDecl");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.DTDHandler a = new org.xml.sax.DTDHandler(){
            public void notationDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void unparsedEntityDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) { }
        };
        check(a != null, "anon impl of DTDHandler");
        a.notationDecl("s", "s", "s");
        a.unparsedEntityDecl("s", "s", "s", "s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DTDHandler contract (notation + unparsed entity) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        xr.setDTDHandler(new org.xml.sax.DTDHandler() {
            public void notationDecl(String name, String publicId, String systemId) { log.add("notation(" + name + "," + publicId + ")"); }
            public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) { log.add("entity(" + name + "," + notationName + ")"); }
        });
        String xml = "<!DOCTYPE r [<!NOTATION gif SYSTEM 'gif.exe'><!ENTITY pic SYSTEM 'p.gif' NDATA gif>]><r/>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(log.size() == 2, "one notation + one unparsed entity declaration reported");
        check(log.get(0).startsWith("notation(gif,"), "notationDecl receives name/publicId");
        check(log.get(1).equals("entity(pic,gif)"), "unparsedEntityDecl receives name and NDATA notation");
        check(xr.getDTDHandler() != null, "getDTDHandler round-trip");
    }
}
