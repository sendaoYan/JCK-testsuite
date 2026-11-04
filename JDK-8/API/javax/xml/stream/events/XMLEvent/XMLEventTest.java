/*
 * @test
 * @summary javax.xml.stream.events.XMLEvent conformance: interface hierarchy and methods.
 * @run main XMLEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLEventTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.XMLEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.XMLStreamConstants"), "implements javax.xml.stream.XMLStreamConstants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEventType"), "has method getEventType");
        check(names.contains("getLocation"), "has method getLocation");
        check(names.contains("isStartElement"), "has method isStartElement");
        check(names.contains("isAttribute"), "has method isAttribute");
        check(names.contains("isNamespace"), "has method isNamespace");
        check(names.contains("isEndElement"), "has method isEndElement");
        check(names.contains("isEntityReference"), "has method isEntityReference");
        check(names.contains("isProcessingInstruction"), "has method isProcessingInstruction");
        check(names.contains("isCharacters"), "has method isCharacters");
        check(names.contains("isStartDocument"), "has method isStartDocument");
        check(names.contains("isEndDocument"), "has method isEndDocument");
        check(names.contains("asStartElement"), "has method asStartElement");
        check(names.contains("asEndElement"), "has method asEndElement");
        check(names.contains("asCharacters"), "has method asCharacters");
        check(names.contains("getSchemaType"), "has method getSchemaType");
        check(names.contains("writeAsEncodedUnicode"), "has method writeAsEncodedUnicode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
