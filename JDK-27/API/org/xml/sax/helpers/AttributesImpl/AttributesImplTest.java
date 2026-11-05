/*
 * @test
 * @summary org.xml.sax.helpers.AttributesImpl conformance: white-box structure + black-box behavior.
 * @run main AttributesImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributesImplTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.AttributesImpl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.Attributes"), "interface org.xml.sax.Attributes");
        check(hasMethod(c, "addAttribute"), "method addAttribute");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "getIndex"), "method getIndex");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getLocalName"), "method getLocalName");
        check(hasMethod(c, "getQName"), "method getQName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getURI"), "method getURI");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "removeAttribute"), "method removeAttribute");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        check(hasMethod(c, "setAttributes"), "method setAttributes");
        check(hasMethod(c, "setLocalName"), "method setLocalName");
        check(hasMethod(c, "setQName"), "method setQName");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "setURI"), "method setURI");
        check(hasMethod(c, "setValue"), "method setValue");

        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        a.addAttribute("uri", "local", "q", "CDATA", "value");
        check(a.getLength() == 1, "length");
        check("value".equals(a.getValue(0)), "value");
        check("local".equals(a.getLocalName(0)), "localName");
        check("q".equals(a.getQName(0)), "qName");
        check("uri".equals(a.getURI(0)), "uri");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        check(a.getLength() == 0, "initially empty");
        a.addAttribute("urn:ns", "local", "p:local", "CDATA", "val");
        check(a.getLength() == 1, "addAttribute increments length");
        check("urn:ns".equals(a.getURI(0)), "getURI");
        check("local".equals(a.getLocalName(0)), "getLocalName");
        check("p:local".equals(a.getQName(0)), "getQName");
        check("CDATA".equals(a.getType(0)), "getType(int)");
        check("val".equals(a.getValue(0)), "getValue(int)");
        check(a.getIndex("p:local") == 0, "getIndex(qName)");
        check(a.getIndex("urn:ns", "local") == 0, "getIndex(uri,local)");
        check("val".equals(a.getValue("p:local")), "getValue(qName)");
        check("val".equals(a.getValue("urn:ns", "local")), "getValue(uri,local)");
        check(a.getType("p:local").equals("CDATA"), "getType(qName)");
        a.setAttribute(0, "urn:2", "l2", "p:l2", "CDATA", "v2");
        check("v2".equals(a.getValue(0)) && "l2".equals(a.getLocalName(0)), "setAttribute(int)");
        a.setValue(0, "v3"); check("v3".equals(a.getValue(0)), "setValue(int)");
        a.removeAttribute(0); check(a.getLength() == 0, "removeAttribute");
        a.addAttribute("", "", "q", "CDATA", "x");
        a.clear(); check(a.getLength() == 0, "clear");
        // copy ctor + setAttributes
        org.xml.sax.helpers.AttributesImpl src = new org.xml.sax.helpers.AttributesImpl();
        src.addAttribute("", "", "k", "CDATA", "v");
        org.xml.sax.helpers.AttributesImpl copy = new org.xml.sax.helpers.AttributesImpl(src);
        check(copy.getLength() == 1 && "v".equals(copy.getValue("k")), "copy ctor");
        org.xml.sax.helpers.AttributesImpl dst = new org.xml.sax.helpers.AttributesImpl();
        dst.setAttributes(src);
        check(dst.getLength() == 1, "setAttributes");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral AttributesImpl contract (mutable SAX2 list full matrix) ----
    static void deepContract() throws Exception {
        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        check(a.getLength() == 0 && a.getIndex("q") == -1, "fresh list empty, lookup -1");
        a.addAttribute("urn:p", "x", "p:x", "CDATA", "1");
        a.addAttribute("", "y", "y", "ID", "2");
        check(a.getLength() == 2 && a.getIndex("p:x") == 0 && a.getIndex("y") == 1, "getIndex by qName");
        check(a.getIndex("urn:p", "x") == 0 && a.getIndex("", "y") == 1 && a.getIndex("urn:z", "y") == -1, "getIndex by uri+localName");
        check("urn:p".equals(a.getURI(0)) && "x".equals(a.getLocalName(0)) && "p:x".equals(a.getQName(0)), "uri/local/qName accessors");
        check("CDATA".equals(a.getType(0)) && "1".equals(a.getValue(0)), "type/value by index");
        check("1".equals(a.getValue("p:x")) && "2".equals(a.getValue("y")), "value by qName");
        check("1".equals(a.getValue("urn:p", "x")), "value by uri+localName");
        check(a.getValue("nope") == null && a.getType("nope") == null, "missing lookups -> null");
        // duplicate qNames coexist; by-qName lookup returns the first
        a.addAttribute("urn:q", "x", "p:x", "NMTOKEN", "9");
        check(a.getLength() == 3, "duplicate qName appended as a distinct entry");
        check("1".equals(a.getValue("p:x")) && "9".equals(a.getValue(2)), "by-qName lookup returns the FIRST entry");
        // copy constructor
        org.xml.sax.helpers.AttributesImpl copy = new org.xml.sax.helpers.AttributesImpl(a);
        check(copy.getLength() == 3 && "9".equals(copy.getValue(2)), "copy constructor snapshots all entries");
        a.removeAttribute(2);
        check(copy.getLength() == 3, "copy independent of later removal");
        // mutators
        a.setValue(1, "22");
        a.setType(1, "IDREF");
        a.setURI(1, "urn:n");
        a.setLocalName(1, "yy");
        a.setQName(1, "n:yy");
        check("22".equals(a.getValue("n:yy")) && "IDREF".equals(a.getType("n:yy")) && "urn:n".equals(a.getURI(1)), "setValue/setType/setURI/setLocalName/setQName round-trip");
        a.clear();
        check(a.getLength() == 0, "clear empties the list");
        a.addAttribute(null, "l", "l", "CDATA", "v");
        // out-of-range matrix: getters -> null, setters/removers -> ArrayIndexOutOfBoundsException
        check(a.getURI(5) == null && a.getValue(5) == null && a.getType(5) == null, "out-of-range getters -> null");
        boolean oobSet = false, oobRm = false, oobVal = false;
        try { a.setAttribute(5, null, "z", "z", "CDATA", "1"); } catch (ArrayIndexOutOfBoundsException e) { oobSet = true; }
        try { a.removeAttribute(5); } catch (ArrayIndexOutOfBoundsException e) { oobRm = true; }
        try { a.setValue(5, "x"); } catch (ArrayIndexOutOfBoundsException e) { oobVal = true; }
        check(oobSet && oobRm && oobVal, "out-of-range mutators -> ArrayIndexOutOfBoundsException");
        // JDK quirk (both sides): a null-URI entry poisons the 2-arg getIndex lookup with an NPE
        org.xml.sax.helpers.AttributesImpl withNull = new org.xml.sax.helpers.AttributesImpl();
        withNull.addAttribute(null, "n", "n", "CDATA", "v");
        boolean nullNpe = false;
        try { withNull.getIndex("", "n"); } catch (NullPointerException e) { nullNpe = true; }
        check(nullNpe || withNull.getIndex("", "n") == 0, "null-URI entry in getIndex(uri,local) observed (NPE=" + nullNpe + ")");
        check(withNull.getIndex("n") == 0, "null-URI entry still reachable by qName lookup");
        // setAttribute replaces in place
        a.setAttribute(0, "urn:r", "l2", "l2", "ENUM", "e2");
        check(a.getLength() == 1 && "e2".equals(a.getValue("l2")) && a.getValue("l") == null, "setAttribute replaces the slot in place");
    }
}
