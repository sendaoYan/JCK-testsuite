/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.MtomCodec.MtomXMLStreamReaderEx conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MtomXMLStreamReaderExTest
 */

import java.lang.reflect.Modifier;


public class MtomXMLStreamReaderExTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.MtomCodec$MtomXMLStreamReaderEx", false, MtomXMLStreamReaderExTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.MtomCodec$MtomXMLStreamReaderEx"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.util.xml.XMLStreamReaderFilter"), "superclass com.sun.xml.internal.ws.util.xml.XMLStreamReaderFilter");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.org.jvnet.staxex.XMLStreamReaderEx"), "implements com.sun.xml.internal.org.jvnet.staxex.XMLStreamReaderEx");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getElementTextTrim"), "method getElementTextTrim");
        check(mn.contains("getEventType"), "method getEventType");
        check(mn.contains("getMimeMultipartParser"), "method getMimeMultipartParser");
        check(mn.contains("getNamespaceContext"), "method getNamespaceContext");
        check(mn.contains("getPCDATA"), "method getPCDATA");
        check(mn.contains("getText"), "method getText");
        check(mn.contains("getTextCharacters"), "method getTextCharacters");
        check(mn.contains("getTextLength"), "method getTextLength");
        check(mn.contains("getTextStart"), "method getTextStart");
        check(mn.contains("getXopHref"), "method getXopHref");
        check(mn.contains("isXopReference"), "method isXopReference");
        check(mn.contains("next"), "method next");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
