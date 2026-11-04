/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.dom.DOMDocumentSerializer conformance: class hierarchy and members.
 * @run main DOMDocumentSerializerTest
 */

public class DOMDocumentSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.dom.DOMDocumentSerializer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.dom.DOMDocumentSerializer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.Encoder".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.Encoder");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("encodeAttribute"), "has method encodeAttribute");
        check(mn.contains("encodeElement"), "has method encodeElement");
        check(mn.contains("serialize"), "has method serialize");
        check(mn.contains("serializeCDATA"), "has method serializeCDATA");
        check(mn.contains("serializeComment"), "has method serializeComment");
        check(mn.contains("serializeElement"), "has method serializeElement");
        check(mn.contains("serializeElementAsDocument"), "has method serializeElementAsDocument");
        check(mn.contains("serializeProcessingInstruction"), "has method serializeProcessingInstruction");
        check(mn.contains("serializeText"), "has method serializeText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
