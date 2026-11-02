/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.saaj.SaajStaxWriter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SaajStaxWriterTest
 */

import java.lang.reflect.Modifier;


public class SaajStaxWriterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.saaj.SaajStaxWriter", false, SaajStaxWriterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.saaj.SaajStaxWriter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.XMLStreamWriter"), "implements javax.xml.stream.XMLStreamWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "method close");
        check(mn.contains("flush"), "method flush");
        check(mn.contains("getNamespaceContext"), "method getNamespaceContext");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("getSOAPMessage"), "method getSOAPMessage");
        check(mn.contains("setDefaultNamespace"), "method setDefaultNamespace");
        check(mn.contains("setNamespaceContext"), "method setNamespaceContext");
        check(mn.contains("setPrefix"), "method setPrefix");
        check(mn.contains("writeAttribute"), "method writeAttribute");
        check(mn.contains("writeCData"), "method writeCData");
        check(mn.contains("writeCharacters"), "method writeCharacters");
        check(mn.contains("writeComment"), "method writeComment");
        check(mn.contains("writeDTD"), "method writeDTD");
        check(mn.contains("writeDefaultNamespace"), "method writeDefaultNamespace");
        check(mn.contains("writeEmptyElement"), "method writeEmptyElement");
        check(mn.contains("writeEndDocument"), "method writeEndDocument");
        check(mn.contains("writeEndElement"), "method writeEndElement");
        check(mn.contains("writeEntityRef"), "method writeEntityRef");
        check(mn.contains("writeNamespace"), "method writeNamespace");
        check(mn.contains("writeProcessingInstruction"), "method writeProcessingInstruction");
        check(mn.contains("writeStartDocument"), "method writeStartDocument");
        check(mn.contains("writeStartElement"), "method writeStartElement");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("Body"), "field Body");
        check(fn.contains("Envelope"), "field Envelope");
        check(fn.contains("Header"), "field Header");
        check(fn.contains("currentElement"), "field currentElement");
        check(fn.contains("deferredElement"), "field deferredElement");
        check(fn.contains("envURI"), "field envURI");
        check(fn.contains("soap"), "field soap");
        check(fn.contains("xmlns"), "field xmlns");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
