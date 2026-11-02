/*
 * @test
 * @summary jdk.internal.util.xml.impl.XMLStreamWriterImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.util.xml.impl:+open
 * @run main XMLStreamWriterImplTest
 */
public class XMLStreamWriterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.impl.XMLStreamWriterImpl", false, XMLStreamWriterImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.xml.impl.XMLStreamWriterImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.util.xml.XMLStreamWriter"), "interface jdk.internal.util.xml.XMLStreamWriter");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 25, "declared methods >= 25");
        check(hasM(ms, "close"), "method close");
        check(hasM(ms, "flush"), "method flush");
        check(hasM(ms, "setDoIndent"), "method setDoIndent");
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
        check(fs.length >= 27, "declared fields >= 27");
        check(hasF(fs, "AMPERSAND"), "field AMPERSAND");
        check(hasF(fs, "CLOSE_EMPTY_ELEMENT"), "field CLOSE_EMPTY_ELEMENT");
        check(hasF(fs, "CLOSE_END_TAG"), "field CLOSE_END_TAG");
        check(hasF(fs, "CLOSE_START_TAG"), "field CLOSE_START_TAG");
        check(hasF(fs, "DOUBLEQUOT"), "field DOUBLEQUOT");
        check(hasF(fs, "ENCODING_PREFIX"), "field ENCODING_PREFIX");
        check(hasF(fs, "END_CDATA"), "field END_CDATA");
        check(hasF(fs, "OPEN_END_TAG"), "field OPEN_END_TAG");
        check(hasF(fs, "OPEN_START_TAG"), "field OPEN_START_TAG");
        check(hasF(fs, "SEMICOLON"), "field SEMICOLON");
        check(hasF(fs, "SPACE"), "field SPACE");
        check(hasF(fs, "START_CDATA"), "field START_CDATA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
