/*
 * @test
 * @summary org.xml.sax.ext.LexicalHandler conformance: white-box structure + black-box behavior.
 * @run main LexicalHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class LexicalHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.ext.LexicalHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "comment"), "method comment");
        check(hasMethod(c, "endCDATA"), "method endCDATA");
        check(hasMethod(c, "endDTD"), "method endDTD");
        check(hasMethod(c, "endEntity"), "method endEntity");
        check(hasMethod(c, "startCDATA"), "method startCDATA");
        check(hasMethod(c, "startDTD"), "method startDTD");
        check(hasMethod(c, "startEntity"), "method startEntity");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ext.LexicalHandler a = new org.xml.sax.ext.LexicalHandler(){
            public void startDTD(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void endDTD() { }
            public void startEntity(java.lang.String p0) { }
            public void endEntity(java.lang.String p0) { }
            public void startCDATA() { }
            public void endCDATA() { }
            public void comment(char[] p0, int p1, int p2) { }
        };
        check(a != null, "anon impl of LexicalHandler");
        a.startDTD("s", "s", "s");
        a.endDTD();
        a.startEntity("s");
        a.endEntity("s");
        a.startCDATA();
        a.endCDATA();
        a.comment((char[]) null, 3, 3);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LexicalHandler contract (DTD/CDATA/entity/comment events) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        org.xml.sax.ext.LexicalHandler lh = new org.xml.sax.ext.LexicalHandler() {
            public void startDTD(String name, String pid, String sid) { log.add("startDTD(" + name + "," + pid + "," + sid + ")"); }
            public void endDTD() { log.add("endDTD"); }
            public void startEntity(String name) { log.add("startEnt(" + name + ")"); }
            public void endEntity(String name) { log.add("endEnt(" + name + ")"); }
            public void startCDATA() { log.add("startCDATA"); }
            public void endCDATA() { log.add("endCDATA"); }
            public void comment(char[] c, int s, int n) { log.add("comment(" + new String(c, s, n) + ")"); }
        };
        xr.setProperty("http://xml.org/sax/properties/lexical-handler", lh);
        check(xr.getProperty("http://xml.org/sax/properties/lexical-handler") == lh, "lexical-handler property round-trip");
        String xml = "<!DOCTYPE r [<!ENTITY ie 'IV'>]><r><!--dc--><![CDATA[cd]]>&ie;<!--tail--></r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(log.get(0).equals("startDTD(r,null,null)"), "startDTD receives name/publicId/systemId");
        check(log.contains("endDTD"), "endDTD pairs with startDTD");
        check(log.contains("comment(dc)") && log.contains("comment(tail)"), "comments inside and after content both reported");
        int sc = log.indexOf("startCDATA");
        int ec = log.indexOf("endCDATA");
        check(sc >= 0 && ec > sc, "startCDATA/endCDATA bracket the CDATA content");
        check(log.contains("startEnt(ie)") && log.contains("endEnt(ie)"), "internal entity expansion wrapped in startEntity/endEntity");
        // comments also appear in the DTD
        final int[] dtdComments = new int[]{0};
        org.xml.sax.ext.LexicalHandler lh2 = new org.xml.sax.ext.LexicalHandler() {
            public void startDTD(String n, String p, String s) { dtdComments[0] = 1; }
            public void endDTD() { }
            public void startEntity(String n) { }
            public void endEntity(String n) { }
            public void startCDATA() { }
            public void endCDATA() { }
            public void comment(char[] c, int s2, int n) { if (dtdComments[0] == 1) dtdComments[0] = 2; }
        };
        org.xml.sax.XMLReader xr2 = spf.newSAXParser().getXMLReader();
        xr2.setProperty("http://xml.org/sax/properties/lexical-handler", lh2);
        xr2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!--dtdc--><!ELEMENT r (#PCDATA)>]><r/>")));
        check(dtdComments[0] == 2, "comments inside the internal DTD subset are reported");
    }
}
