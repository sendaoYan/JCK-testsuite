/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.stax.StreamReaderBufferProcessor conformance: class hierarchy and members.
 * @run main StreamReaderBufferProcessorTest
 */

public class StreamReaderBufferProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.stax.StreamReaderBufferProcessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.stax.StreamReaderBufferProcessor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.AbstractProcessor".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.AbstractProcessor");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.staxex.XMLStreamReaderEx"), "has interface com.sun.xml.internal.org.jvnet.staxex.XMLStreamReaderEx");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("fixEmptyString"), "has method fixEmptyString");
        check(mn.contains("fixNull"), "has method fixNull");
        check(mn.contains("getAttributeCount"), "has method getAttributeCount");
        check(mn.contains("getAttributeLocalName"), "has method getAttributeLocalName");
        check(mn.contains("getAttributeName"), "has method getAttributeName");
        check(mn.contains("getAttributeNamespace"), "has method getAttributeNamespace");
        check(mn.contains("getAttributePrefix"), "has method getAttributePrefix");
        check(mn.contains("getAttributeType"), "has method getAttributeType");
        check(mn.contains("getAttributeValue"), "has method getAttributeValue");
        check(mn.contains("getCharacterEncodingScheme"), "has method getCharacterEncodingScheme");
        check(mn.contains("getElementText"), "has method getElementText");
        check(mn.contains("getElementTextTrim"), "has method getElementTextTrim");
        check(mn.contains("getEncoding"), "has method getEncoding");
        check(mn.contains("getEventType"), "has method getEventType");
        check(mn.contains("getLocalName"), "has method getLocalName");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getNamespaceCount"), "has method getNamespaceCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
