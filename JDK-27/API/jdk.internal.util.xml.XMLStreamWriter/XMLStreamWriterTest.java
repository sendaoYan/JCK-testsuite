/*
 * @test
 * @summary jdk.internal.util.xml.XMLStreamWriter conformance: internal interface reflection.
 * @modules java.base/jdk.internal.util.xml:+open
 * @run main XMLStreamWriterTest
 */
public class XMLStreamWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.XMLStreamWriter", false, XMLStreamWriterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.xml.XMLStreamWriter"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "close"), "method close");
        check(hasM(ms, "flush"), "method flush");
        check(hasM(ms, "writeAttribute"), "method writeAttribute");
        check(hasM(ms, "writeCData"), "method writeCData");
        check(hasM(ms, "writeCharacters"), "method writeCharacters");
        check(hasM(ms, "writeDTD"), "method writeDTD");
        check(hasM(ms, "writeEmptyElement"), "method writeEmptyElement");
        check(hasM(ms, "writeEndDocument"), "method writeEndDocument");
        check(hasM(ms, "writeEndElement"), "method writeEndElement");
        check(hasM(ms, "writeStartDocument"), "method writeStartDocument");
        check(hasM(ms, "writeStartElement"), "method writeStartElement");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        check(hasF(fs, "DEFAULT_CHARSET"), "field DEFAULT_CHARSET");
        check(hasF(fs, "DEFAULT_ENCODING"), "field DEFAULT_ENCODING");
        check(hasF(fs, "DEFAULT_XML_VERSION"), "field DEFAULT_XML_VERSION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
