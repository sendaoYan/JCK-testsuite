/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.StAXDocumentSerializer conformance: class hierarchy and members.
 * @run main StAXDocumentSerializerTest
 */

public class StAXDocumentSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.StAXDocumentSerializer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.StAXDocumentSerializer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.Encoder".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.Encoder");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.stax.LowLevelFastInfosetStreamWriter"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.stax.LowLevelFastInfosetStreamWriter");
        check(ifs.contains("javax.xml.stream.XMLStreamWriter"), "has interface javax.xml.stream.XMLStreamWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("encodeAttribute"), "has method encodeAttribute");
        check(mn.contains("encodeElement"), "has method encodeElement");
        check(mn.contains("encodeLiteral"), "has method encodeLiteral");
        check(mn.contains("encodeLiteralHeader"), "has method encodeLiteralHeader");
        check(mn.contains("encodeTerminationAndCurrentElement"), "has method encodeTerminationAndCurrentElement");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("getLocalNameIndex"), "has method getLocalNameIndex");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getNextAttributeIndex"), "has method getNextAttributeIndex");
        check(mn.contains("getNextElementIndex"), "has method getNextElementIndex");
        check(mn.contains("getNextLocalNameIndex"), "has method getNextLocalNameIndex");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("initiateLowLevelWriting"), "has method initiateLowLevelWriting");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(mn.contains("setEncoding"), "has method setEncoding");
        check(mn.contains("setManager"), "has method setManager");
        check(mn.contains("setNamespaceContext"), "has method setNamespaceContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
