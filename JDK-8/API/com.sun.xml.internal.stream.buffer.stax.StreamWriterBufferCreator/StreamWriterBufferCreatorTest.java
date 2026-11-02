/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferCreator conformance: class hierarchy and members.
 * @run main StreamWriterBufferCreatorTest
 */

public class StreamWriterBufferCreatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferCreator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferCreator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.stax.StreamBufferCreator".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.stax.StreamBufferCreator");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.staxex.XMLStreamWriterEx"), "has interface com.sun.xml.internal.org.jvnet.staxex.XMLStreamWriterEx");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(mn.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(mn.contains("setPrefix"), "has method setPrefix");
        check(mn.contains("writeAttribute"), "has method writeAttribute");
        check(mn.contains("writeBinary"), "has method writeBinary");
        check(mn.contains("writeCData"), "has method writeCData");
        check(mn.contains("writeCharacters"), "has method writeCharacters");
        check(mn.contains("writeComment"), "has method writeComment");
        check(mn.contains("writeDTD"), "has method writeDTD");
        check(mn.contains("writeDefaultNamespace"), "has method writeDefaultNamespace");
        check(mn.contains("writeEmptyElement"), "has method writeEmptyElement");
        check(mn.contains("writeEndDocument"), "has method writeEndDocument");
        check(mn.contains("writeEndElement"), "has method writeEndElement");
        check(mn.contains("writeEntityRef"), "has method writeEntityRef");
        check(mn.contains("writeNamespace"), "has method writeNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
