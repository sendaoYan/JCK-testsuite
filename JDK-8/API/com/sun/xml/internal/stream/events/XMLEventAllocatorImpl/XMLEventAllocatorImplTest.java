/*
 * @test
 * @summary com.sun.xml.internal.stream.events.XMLEventAllocatorImpl conformance: class hierarchy and members.
 * @run main XMLEventAllocatorImplTest
 */

public class XMLEventAllocatorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.XMLEventAllocatorImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.XMLEventAllocatorImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.util.XMLEventAllocator"), "has interface javax.xml.stream.util.XMLEventAllocator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("allocate"), "has method allocate");
        check(mn.contains("fillAttributes"), "has method fillAttributes");
        check(mn.contains("fillNamespaceAttributes"), "has method fillNamespaceAttributes");
        check(mn.contains("getNextEvent"), "has method getNextEvent");
        check(mn.contains("getQName"), "has method getQName");
        check(mn.contains("getXMLEvent"), "has method getXMLEvent");
        check(mn.contains("newInstance"), "has method newInstance");
        check(mn.contains("setNamespaceContext"), "has method setNamespaceContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
