/*
 * @test
 * @summary jdk.javadoc.doclet.Reporter conformance: white-box structure + black-box behavior.
 * @run main ReporterTest
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
public class ReporterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.doclet.Reporter");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "print"), "print");
        check(hasMethod(c, "getStandardWriter"), "getStandardWriter");
        check(hasMethod(c, "getDiagnosticWriter"), "getDiagnosticWriter");

    }

    static void testBlackBox() throws Exception {
        Reporter rep = new Reporter() {
            public void print(Diagnostic.Kind k, String m) {}
            public void print(Diagnostic.Kind k, DocTreePath p, String m) {}
            public void print(Diagnostic.Kind k, Element e, String m) {}
        };
        rep.print(Diagnostic.Kind.NOTE, "hello");
        rep.print(Diagnostic.Kind.NOTE, (Element) null, "via element");
        rep.getStandardWriter();
        rep.getDiagnosticWriter();
        check(true, "default writers callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
