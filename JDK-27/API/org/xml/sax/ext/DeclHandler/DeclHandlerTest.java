/*
 * @test
 * @summary org.xml.sax.ext.DeclHandler conformance: white-box structure + black-box behavior.
 * @run main DeclHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DeclHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.ext.DeclHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "attributeDecl"), "method attributeDecl");
        check(hasMethod(c, "elementDecl"), "method elementDecl");
        check(hasMethod(c, "externalEntityDecl"), "method externalEntityDecl");
        check(hasMethod(c, "internalEntityDecl"), "method internalEntityDecl");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ext.DeclHandler a = new org.xml.sax.ext.DeclHandler(){
            public void elementDecl(java.lang.String p0, java.lang.String p1) { }
            public void attributeDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3, java.lang.String p4) { }
            public void internalEntityDecl(java.lang.String p0, java.lang.String p1) { }
            public void externalEntityDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
        };
        check(a != null, "anon impl of DeclHandler");
        a.elementDecl("s", "s");
        a.attributeDecl("s", "s", "s", "s", "s");
        a.internalEntityDecl("s", "s");
        a.externalEntityDecl("s", "s", "s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DeclHandler contract (DTD declaration callbacks) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        org.xml.sax.ext.DeclHandler dh = new org.xml.sax.ext.DeclHandler() {
            public void elementDecl(String name, String model) { log.add("elem(" + name + "=" + model + ")"); }
            public void attributeDecl(String eName, String aName, String type, String mode, String value) { log.add("att(" + aName + "," + type + "," + mode + "," + value + ")"); }
            public void internalEntityDecl(String name, String value) { log.add("ient(" + name + "=" + value + ")"); }
            public void externalEntityDecl(String name, String publicId, String systemId) { log.add("eent(" + name + ")"); }
        };
        xr.setProperty("http://xml.org/sax/properties/declaration-handler", dh);
        check(xr.getProperty("http://xml.org/sax/properties/declaration-handler") == dh, "declaration-handler property round-trip");
        String dtd = "<!DOCTYPE r [<!ELEMENT r (a*)><!ELEMENT a (#PCDATA)><!ATTLIST r x CDATA 'dx' y CDATA #REQUIRED z CDATA #FIXED 'fx'><!ENTITY ie 'iv'><!ENTITY ee SYSTEM 'e.xml'>]><r y='1'><a>t</a></r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(dtd)));
        check(log.contains("elem(r=(a*))"), "elementDecl receives name and content model");
        check(log.contains("att(x,CDATA,null,dx)"), "defaulted attribute: attributeDecl reports value with a null mode");
        check(log.contains("att(y,CDATA,#REQUIRED,null)"), "#REQUIRED attribute: mode set, value null");
        check(log.contains("att(z,CDATA,#FIXED,fx)"), "#FIXED attribute: mode and fixed value");
        check(log.contains("ient(ie=iv)"), "internalEntityDecl receives the replacement text");
        check(log.contains("eent(ee)"), "externalEntityDecl fires for the external entity");
        // removing the handler stops the callbacks
        xr.setProperty("http://xml.org/sax/properties/declaration-handler", null);
        check(xr.getProperty("http://xml.org/sax/properties/declaration-handler") == null, "clearing the property works");
    }
}
