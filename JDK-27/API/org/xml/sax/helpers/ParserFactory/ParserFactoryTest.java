/*
 * @test
 * @summary org.xml.sax.helpers.ParserFactory conformance: white-box structure + black-box behavior.
 * @run main ParserFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ParserFactoryTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.ParserFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "makeParser"), "method makeParser");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        boolean ok = false;
        try { org.xml.sax.helpers.ParserFactory.makeParser(); }
        catch (NullPointerException e) { ok = true; }
        check(ok, "makeParser() without property -> NullPointerException");
        check(org.xml.sax.helpers.ParserFactory.makeParser("com.sun.org.apache.xerces.internal.parsers.SAXParser") != null, "makeParser(className)");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ParserFactory contract (deprecated SAX1 bootstrap) ----
    static void deepContract() throws Exception {
        boolean deprecated = org.xml.sax.helpers.ParserFactory.class.getAnnotation(Deprecated.class) != null;
        boolean is8 = System.getProperty("java.version").startsWith("1.8");
        check(is8 ? !deprecated : deprecated, "ParserFactory deprecation annotation differs by JDK (8: javadoc-only, 27+: @Deprecated present; observed=" + deprecated + ")");
        boolean npe = false;
        String msg = null;
        try { org.xml.sax.helpers.ParserFactory.makeParser(); }
        catch (NullPointerException e) { npe = true; msg = e.getMessage(); }
        check(npe && msg != null && msg.contains("sax.parser"), "makeParser() without org.xml.sax.parser -> NPE mentioning sax.parser (msg=" + msg + ")");
        boolean cnf = false;
        try { org.xml.sax.helpers.ParserFactory.makeParser("no.such.Class"); }
        catch (ClassNotFoundException e) { cnf = true; }
        check(cnf, "makeParser(unknown class) -> ClassNotFoundException");
    }
}
