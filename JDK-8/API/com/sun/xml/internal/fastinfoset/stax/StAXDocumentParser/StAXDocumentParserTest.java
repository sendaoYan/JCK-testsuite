/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser conformance: class hierarchy and members.
 * @run main StAXDocumentParserTest
 */

public class StAXDocumentParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.Decoder".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.Decoder");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.fastinfoset.OctetBufferListener"), "has interface com.sun.xml.internal.fastinfoset.OctetBufferListener");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.stax.FastInfosetStreamReader"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.stax.FastInfosetStreamReader");
        check(ifs.contains("javax.xml.stream.XMLStreamReader"), "has interface javax.xml.stream.XMLStreamReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("accessLocalName"), "has method accessLocalName");
        check(mn.contains("accessNamespaceCount"), "has method accessNamespaceCount");
        check(mn.contains("accessNamespaceURI"), "has method accessNamespaceURI");
        check(mn.contains("accessPrefix"), "has method accessPrefix");
        check(mn.contains("accessTextCharacters"), "has method accessTextCharacters");
        check(mn.contains("accessTextLength"), "has method accessTextLength");
        check(mn.contains("accessTextStart"), "has method accessTextStart");
        check(mn.contains("base64DecodeWithCloning"), "has method base64DecodeWithCloning");
        check(mn.contains("base64DecodeWithoutCloning"), "has method base64DecodeWithoutCloning");
        check(mn.contains("checkTextState"), "has method checkTextState");
        check(mn.contains("close"), "has method close");
        check(mn.contains("convertBase64AlorithmDataToCharacters"), "has method convertBase64AlorithmDataToCharacters");
        check(mn.contains("convertEncodingAlgorithmDataToCharacters"), "has method convertEncodingAlgorithmDataToCharacters");
        check(mn.contains("getAttributeCount"), "has method getAttributeCount");
        check(mn.contains("getAttributeLocalName"), "has method getAttributeLocalName");
        check(mn.contains("getAttributeName"), "has method getAttributeName");
        check(mn.contains("getAttributeNameString"), "has method getAttributeNameString");
        check(mn.contains("getAttributeNamespace"), "has method getAttributeNamespace");
        check(mn.contains("getAttributePrefix"), "has method getAttributePrefix");
        check(mn.contains("getAttributeType"), "has method getAttributeType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
