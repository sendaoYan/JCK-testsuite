/*
 * @test
 * @summary com.sun.xml.internal.stream.StaxXMLInputSource conformance: class hierarchy and members.
 * @run main StaxXMLInputSourceTest
 */

public class StaxXMLInputSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.StaxXMLInputSource", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.StaxXMLInputSource"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getXMLEventReader"), "has method getXMLEventReader");
        check(mn.contains("getXMLInputSource"), "has method getXMLInputSource");
        check(mn.contains("getXMLStreamReader"), "has method getXMLStreamReader");
        check(mn.contains("hasXMLStreamOrXMLEventReader"), "has method hasXMLStreamOrXMLEventReader");
        check(mn.contains("isCreatedByResolver"), "has method isCreatedByResolver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
