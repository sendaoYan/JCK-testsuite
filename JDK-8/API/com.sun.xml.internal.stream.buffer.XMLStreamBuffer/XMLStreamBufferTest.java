/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.XMLStreamBuffer conformance: abstract class hierarchy and members.
 * @run main XMLStreamBufferTest
 */

public class XMLStreamBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.XMLStreamBuffer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.XMLStreamBuffer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createNewBufferFromXMLReader"), "has method createNewBufferFromXMLReader");
        check(mn.contains("createNewBufferFromXMLStreamReader"), "has method createNewBufferFromXMLStreamReader");
        check(mn.contains("getContentCharactersBuffer"), "has method getContentCharactersBuffer");
        check(mn.contains("getContentCharactersBufferPtr"), "has method getContentCharactersBufferPtr");
        check(mn.contains("getContentObjects"), "has method getContentObjects");
        check(mn.contains("getContentObjectsPtr"), "has method getContentObjectsPtr");
        check(mn.contains("getInscopeNamespaces"), "has method getInscopeNamespaces");
        check(mn.contains("getStructure"), "has method getStructure");
        check(mn.contains("getStructurePtr"), "has method getStructurePtr");
        check(mn.contains("getStructureStrings"), "has method getStructureStrings");
        check(mn.contains("getStructureStringsPtr"), "has method getStructureStringsPtr");
        check(mn.contains("getSystemId"), "has method getSystemId");
        check(mn.contains("hasInternedStrings"), "has method hasInternedStrings");
        check(mn.contains("isCreated"), "has method isCreated");
        check(mn.contains("isElementFragment"), "has method isElementFragment");
        check(mn.contains("isForest"), "has method isForest");
        check(mn.contains("isFragment"), "has method isFragment");
        check(mn.contains("readAsXMLReader"), "has method readAsXMLReader");
        check(mn.contains("readAsXMLStreamReader"), "has method readAsXMLStreamReader");
        check(mn.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
