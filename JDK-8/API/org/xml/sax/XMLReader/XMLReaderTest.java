/*
 * @test
 * @summary org.xml.sax.XMLReader conformance: interface hierarchy and methods.
 * @run main XMLReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.xml.sax.XMLReader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getFeature"), "has method getFeature");
        check(names.contains("setFeature"), "has method setFeature");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("setEntityResolver"), "has method setEntityResolver");
        check(names.contains("getEntityResolver"), "has method getEntityResolver");
        check(names.contains("setDTDHandler"), "has method setDTDHandler");
        check(names.contains("getDTDHandler"), "has method getDTDHandler");
        check(names.contains("setContentHandler"), "has method setContentHandler");
        check(names.contains("getContentHandler"), "has method getContentHandler");
        check(names.contains("setErrorHandler"), "has method setErrorHandler");
        check(names.contains("getErrorHandler"), "has method getErrorHandler");
        check(names.contains("parse"), "has method parse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
