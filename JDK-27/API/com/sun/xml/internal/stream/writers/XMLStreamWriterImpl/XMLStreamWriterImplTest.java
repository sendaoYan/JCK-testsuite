/*
 * @test
 * @summary com.sun.xml.internal.stream.writers.XMLStreamWriterImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.writers:+open
 * @run main XMLStreamWriterImplTest
 */
public class XMLStreamWriterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.writers.XMLStreamWriterImpl", false, XMLStreamWriterImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.writers.XMLStreamWriterImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractMap"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.xml.internal.stream.writers.XMLStreamWriterBase"), "if-com.sun.xml.internal.stream.writers.XMLStreamWriterBase");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 15, "field-count");
        check(fd.contains("START_COMMENT"), "field-START_COMMENT");
        check(fd.contains("END_COMMENT"), "field-END_COMMENT");
        check(fd.contains("DEFAULT_ENCODING"), "field-DEFAULT_ENCODING");
        check(fd.contains("DEFAULT_XMLDECL"), "field-DEFAULT_XMLDECL");
        check(fd.contains("DEFAULT_XML_VERSION"), "field-DEFAULT_XML_VERSION");
        check(fd.contains("CLOSE_START_TAG"), "field-CLOSE_START_TAG");
        check(fd.contains("OPEN_START_TAG"), "field-OPEN_START_TAG");
        check(fd.contains("OPEN_END_TAG"), "field-OPEN_END_TAG");
        check(fd.contains("CLOSE_END_TAG"), "field-CLOSE_END_TAG");
        check(fd.contains("START_CDATA"), "field-START_CDATA");
        check(fd.contains("END_CDATA"), "field-END_CDATA");
        check(fd.contains("CLOSE_EMPTY_ELEMENT"), "field-CLOSE_EMPTY_ELEMENT");
        check(fd.contains("SPACE"), "field-SPACE");
        check(fd.contains("UTF_8"), "field-UTF_8");
        check(fd.contains("OUTPUTSTREAM_PROPERTY"), "field-OUTPUTSTREAM_PROPERTY");
        check(md.size() == 36, "method-count");
        check(md.contains("canReuse"), "method-canReuse");
        check(md.contains("close"), "method-close");
        check(md.contains("containsKey"), "method-containsKey");
        check(md.contains("entrySet"), "method-entrySet");
        check(md.contains("equals"), "method-equals");
        check(md.contains("flush"), "method-flush");
        check(md.contains("get"), "method-get");
        check(md.contains("getEscapeCharacters"), "method-getEscapeCharacters");
        check(md.contains("getNamespaceContext"), "method-getNamespaceContext");
        check(md.contains("getPrefix"), "method-getPrefix");
        check(md.contains("getProperty"), "method-getProperty");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("isEmpty"), "method-isEmpty");
        check(md.contains("repair"), "method-repair");
        check(md.contains("reset"), "method-reset");
        check(md.contains("setDefaultNamespace"), "method-setDefaultNamespace");
        check(md.contains("setEscapeCharacters"), "method-setEscapeCharacters");
        check(md.contains("setNamespaceContext"), "method-setNamespaceContext");
        check(md.contains("setOutput"), "method-setOutput");
        check(md.contains("setPrefix"), "method-setPrefix");
        check(md.contains("size"), "method-size");
        check(md.contains("toString"), "method-toString");
        check(md.contains("writeAttribute"), "method-writeAttribute");
        check(md.contains("writeCData"), "method-writeCData");
        check(md.contains("writeCharacters"), "method-writeCharacters");
        check(md.contains("writeComment"), "method-writeComment");
        check(md.contains("writeDTD"), "method-writeDTD");
        check(md.contains("writeDefaultNamespace"), "method-writeDefaultNamespace");
        check(md.contains("writeEmptyElement"), "method-writeEmptyElement");
        check(md.contains("writeEndDocument"), "method-writeEndDocument");
        check(md.contains("writeEndElement"), "method-writeEndElement");
        check(md.contains("writeEntityRef"), "method-writeEntityRef");
        check(md.contains("writeNamespace"), "method-writeNamespace");
        check(md.contains("writeProcessingInstruction"), "method-writeProcessingInstruction");
        check(md.contains("writeStartDocument"), "method-writeStartDocument");
        check(md.contains("writeStartElement"), "method-writeStartElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
