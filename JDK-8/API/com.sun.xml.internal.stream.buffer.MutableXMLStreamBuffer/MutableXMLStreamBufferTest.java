/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.MutableXMLStreamBuffer conformance: class hierarchy and members.
 * @run main MutableXMLStreamBufferTest
 */

public class MutableXMLStreamBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.MutableXMLStreamBuffer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.MutableXMLStreamBuffer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.XMLStreamBuffer".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.XMLStreamBuffer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createFromSAXBufferCreator"), "has method createFromSAXBufferCreator");
        check(mn.contains("createFromXMLReader"), "has method createFromXMLReader");
        check(mn.contains("createFromXMLStreamReader"), "has method createFromXMLStreamReader");
        check(mn.contains("createFromXMLStreamWriter"), "has method createFromXMLStreamWriter");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("setHasInternedStrings"), "has method setHasInternedStrings");
        check(mn.contains("setSystemId"), "has method setSystemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
