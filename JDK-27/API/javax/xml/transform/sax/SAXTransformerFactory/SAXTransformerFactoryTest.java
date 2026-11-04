/*
 * @test
 * @summary javax.xml.transform.sax.SAXTransformerFactory conformance: white-box structure + black-box behavior.
 * @run main SAXTransformerFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXTransformerFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.transform.sax.SAXTransformerFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass().getName().equals("javax.xml.transform.TransformerFactory"), "superclass javax.xml.transform.TransformerFactory");
        check(hasMethod(c, "newTemplatesHandler"), "method newTemplatesHandler");
        check(hasMethod(c, "newTransformerHandler"), "method newTransformerHandler");
        check(hasMethod(c, "newXMLFilter"), "method newXMLFilter");
        check(hasField(c, "FEATURE"), "field FEATURE");
        check(hasField(c, "FEATURE_XMLFILTER"), "field FEATURE_XMLFILTER");

        javax.xml.transform.sax.SAXTransformerFactory f =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        check(f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE), "feature");
        check(f.newTransformerHandler() != null, "newTransformerHandler");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        check(f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE) == true
              || f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE) == false, "SAX feature queryable");
        boolean sax = f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE);
        if (sax) {
            javax.xml.transform.sax.SAXTransformerFactory stf = (javax.xml.transform.sax.SAXTransformerFactory) f;
            javax.xml.transform.sax.TransformerHandler th = stf.newTransformerHandler();
            check(th != null, "newTransformerHandler");
            th.getTransformer().reset();
            javax.xml.transform.sax.TemplatesHandler tsh = stf.newTemplatesHandler();
            check(tsh != null, "newTemplatesHandler");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
