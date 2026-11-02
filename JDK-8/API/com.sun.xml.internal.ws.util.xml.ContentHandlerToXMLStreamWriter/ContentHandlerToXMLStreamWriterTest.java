/*
 * @test
 * @summary com.sun.xml.internal.ws.util.xml.ContentHandlerToXMLStreamWriter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ContentHandlerToXMLStreamWriterTest
 */

import java.lang.reflect.Modifier;


public class ContentHandlerToXMLStreamWriterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.xml.ContentHandlerToXMLStreamWriter", false, ContentHandlerToXMLStreamWriterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.xml.ContentHandlerToXMLStreamWriter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass org.xml.sax.helpers.DefaultHandler");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("characters"), "method characters");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("endPrefixMapping"), "method endPrefixMapping");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("setDocumentLocator"), "method setDocumentLocator");
        check(mn.contains("skippedEntity"), "method skippedEntity");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
