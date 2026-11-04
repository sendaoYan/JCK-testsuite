/*
 * @test
 * @summary jdk.javadoc.doclet.StandardDoclet conformance: white-box structure + black-box behavior.
 * @run main StandardDocletTest
 */

import jdk.javadoc.doclet.*;
import javax.tools.*;
import javax.lang.model.*;
import javax.lang.model.element.Element;
import com.sun.source.doctree.*;
import com.sun.source.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
public class StandardDocletTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.doclet.StandardDoclet");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Doclet.class.isAssignableFrom(c), "implements Doclet");
        check(hasMethod(c, "init"), "init");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getSupportedOptions"), "getSupportedOptions");
        check(hasMethod(c, "getSupportedSourceVersion"), "getSupportedSourceVersion");
        check(hasMethod(c, "run"), "run");
        check(hasMethod(c, "getLocale"), "getLocale");
        check(hasMethod(c, "getReporter"), "getReporter");

    }

    static void testBlackBox() throws Exception {
        Reporter rep = new Reporter() {
            public void print(Diagnostic.Kind k, String m) {}
            public void print(Diagnostic.Kind k, DocTreePath p, String m) {}
            public void print(Diagnostic.Kind k, Element e, String m) {}
        };
        StandardDoclet d = new StandardDoclet();
        d.init(Locale.ENGLISH, rep);
        check("Standard".equals(d.getName()), "getName");
        check(d.getLocale() == Locale.ENGLISH, "getLocale");
        check(d.getReporter() == rep, "getReporter");
        check(d.getSupportedSourceVersion() != null, "getSupportedSourceVersion");
        check(d.getSupportedOptions() != null, "getSupportedOptions");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
