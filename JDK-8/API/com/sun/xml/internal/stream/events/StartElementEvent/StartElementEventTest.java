/*
 * @test
 * @summary com.sun.xml.internal.stream.events.StartElementEvent conformance: class hierarchy and members.
 * @run main StartElementEventTest
 */

public class StartElementEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.StartElementEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.StartElementEvent"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.events.DummyEvent".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.events.DummyEvent");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.StartElement"), "has interface javax.xml.stream.events.StartElement");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAttribute"), "has method addAttribute");
        check(mn.contains("addAttributes"), "has method addAttributes");
        check(mn.contains("addNamespaceAttribute"), "has method addNamespaceAttribute");
        check(mn.contains("addNamespaceAttributes"), "has method addNamespaceAttributes");
        check(mn.contains("getAttributeByName"), "has method getAttributeByName");
        check(mn.contains("getAttributes"), "has method getAttributes");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getNamespace"), "has method getNamespace");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(mn.contains("getNamespaces"), "has method getNamespaces");
        check(mn.contains("init"), "has method init");
        check(mn.contains("nameAsString"), "has method nameAsString");
        check(mn.contains("setName"), "has method setName");
        check(mn.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("writeAsEncodedUnicodeEx"), "has method writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
