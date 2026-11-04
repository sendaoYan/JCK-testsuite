/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.writer.TXWContentHandler conformance: reflection (kind, superclass, interfaces, methods).
 * @run main TXWContentHandlerTest
 */

import java.lang.reflect.Modifier;


public class TXWContentHandlerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.writer.TXWContentHandler", false, TXWContentHandlerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.writer.TXWContentHandler"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.ContentHandler"), "implements org.xml.sax.ContentHandler");
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
