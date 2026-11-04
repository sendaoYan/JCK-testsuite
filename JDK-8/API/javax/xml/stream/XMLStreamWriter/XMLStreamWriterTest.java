/*
 * @test
 * @summary javax.xml.stream.XMLStreamWriter conformance: interface hierarchy and methods.
 * @run main XMLStreamWriterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLStreamWriterTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamWriter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("writeStartElement"), "has method writeStartElement");
        check(names.contains("writeEmptyElement"), "has method writeEmptyElement");
        check(names.contains("writeEndElement"), "has method writeEndElement");
        check(names.contains("writeEndDocument"), "has method writeEndDocument");
        check(names.contains("close"), "has method close");
        check(names.contains("flush"), "has method flush");
        check(names.contains("writeAttribute"), "has method writeAttribute");
        check(names.contains("writeNamespace"), "has method writeNamespace");
        check(names.contains("writeDefaultNamespace"), "has method writeDefaultNamespace");
        check(names.contains("writeComment"), "has method writeComment");
        check(names.contains("writeProcessingInstruction"), "has method writeProcessingInstruction");
        check(names.contains("writeCData"), "has method writeCData");
        check(names.contains("writeDTD"), "has method writeDTD");
        check(names.contains("writeEntityRef"), "has method writeEntityRef");
        check(names.contains("writeStartDocument"), "has method writeStartDocument");
        check(names.contains("writeCharacters"), "has method writeCharacters");
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("setPrefix"), "has method setPrefix");
        check(names.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(names.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(names.contains("getProperty"), "has method getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
