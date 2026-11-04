/*
 * @test
 * @summary com.sun.xml.internal.stream.events.NamedEvent conformance: class hierarchy and members.
 * @run main NamedEventTest
 */

public class NamedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.NamedEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.NamedEvent"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.events.DummyEvent".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.events.DummyEvent");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getNamespace"), "has method getNamespace");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("nameAsString"), "has method nameAsString");
        check(mn.contains("setName"), "has method setName");
        check(mn.contains("writeAsEncodedUnicodeEx"), "has method writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
