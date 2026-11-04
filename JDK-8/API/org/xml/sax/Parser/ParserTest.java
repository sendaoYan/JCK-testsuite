/*
 * @test
 * @summary org.xml.sax.Parser conformance: interface hierarchy and methods.
 * @run main ParserTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ParserTest {
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
        Class<?> c = Class.forName("org.xml.sax.Parser");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setLocale"), "has method setLocale");
        check(names.contains("setEntityResolver"), "has method setEntityResolver");
        check(names.contains("setDTDHandler"), "has method setDTDHandler");
        check(names.contains("setDocumentHandler"), "has method setDocumentHandler");
        check(names.contains("setErrorHandler"), "has method setErrorHandler");
        check(names.contains("parse"), "has method parse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
