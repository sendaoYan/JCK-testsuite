/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializerWithPrefixMapping conformance: class hierarchy and members.
 * @run main SAXDocumentSerializerWithPrefixMappingTest
 */

public class SAXDocumentSerializerWithPrefixMappingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializerWithPrefixMapping", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializerWithPrefixMapping"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertQName"), "has method convertQName");
        check(mn.contains("encodeAttribute"), "has method encodeAttribute");
        check(mn.contains("encodeAttributeMapEntry"), "has method encodeAttributeMapEntry");
        check(mn.contains("encodeAttributes"), "has method encodeAttributes");
        check(mn.contains("encodeElement"), "has method encodeElement");
        check(mn.contains("encodeElementMapEntry"), "has method encodeElementMapEntry");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("putPrefix"), "has method putPrefix");
        check(mn.contains("startPrefixMapping"), "has method startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
