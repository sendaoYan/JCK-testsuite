/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.writer.WSDLPatcher conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLPatcherTest
 */

import java.lang.reflect.Modifier;


public class WSDLPatcherTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.writer.WSDLPatcher", false, WSDLPatcherTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.writer.WSDLPatcher"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.util.xml.XMLStreamReaderToXMLStreamWriter"), "superclass com.sun.xml.internal.ws.util.xml.XMLStreamReaderToXMLStreamWriter");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("handleAttribute"), "method handleAttribute");
        check(mn.contains("handleCharacters"), "method handleCharacters");
        check(mn.contains("handleEndElement"), "method handleEndElement");
        check(mn.contains("handleStartElement"), "method handleStartElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
