/*
 * @test
 * @summary org.xml.sax.DocumentHandler conformance: interface hierarchy and methods.
 * @run main DocumentHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DocumentHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.DocumentHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setDocumentLocator"), "has method setDocumentLocator");
        check(names.contains("startDocument"), "has method startDocument");
        check(names.contains("endDocument"), "has method endDocument");
        check(names.contains("startElement"), "has method startElement");
        check(names.contains("endElement"), "has method endElement");
        check(names.contains("characters"), "has method characters");
        check(names.contains("ignorableWhitespace"), "has method ignorableWhitespace");
        check(names.contains("processingInstruction"), "has method processingInstruction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
