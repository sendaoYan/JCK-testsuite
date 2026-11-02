/*
 * @test
 * @summary com.sun.xml.internal.ws.util.xml.XMLStreamReaderToXMLStreamWriter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XMLStreamReaderToXMLStreamWriterTest
 */

import java.lang.reflect.Modifier;


public class XMLStreamReaderToXMLStreamWriterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.xml.XMLStreamReaderToXMLStreamWriter", false, XMLStreamReaderToXMLStreamWriterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.xml.XMLStreamReaderToXMLStreamWriter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("bridge"), "method bridge");
        check(mn.contains("handleAttribute"), "method handleAttribute");
        check(mn.contains("handleCDATA"), "method handleCDATA");
        check(mn.contains("handleCharacters"), "method handleCharacters");
        check(mn.contains("handleComment"), "method handleComment");
        check(mn.contains("handleDTD"), "method handleDTD");
        check(mn.contains("handleEndElement"), "method handleEndElement");
        check(mn.contains("handleEntityReference"), "method handleEntityReference");
        check(mn.contains("handlePI"), "method handlePI");
        check(mn.contains("handleSpace"), "method handleSpace");
        check(mn.contains("handleStartElement"), "method handleStartElement");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("in"), "field in");
        check(fn.contains("out"), "field out");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
