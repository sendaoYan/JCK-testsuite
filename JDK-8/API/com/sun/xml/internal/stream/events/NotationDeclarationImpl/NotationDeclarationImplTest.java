/*
 * @test
 * @summary com.sun.xml.internal.stream.events.NotationDeclarationImpl conformance: class hierarchy and members.
 * @run main NotationDeclarationImplTest
 */

public class NotationDeclarationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.NotationDeclarationImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.NotationDeclarationImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.events.DummyEvent".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.events.DummyEvent");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.NotationDeclaration"), "has interface javax.xml.stream.events.NotationDeclaration");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getPublicId"), "has method getPublicId");
        check(mn.contains("getSystemId"), "has method getSystemId");
        check(mn.contains("setName"), "has method setName");
        check(mn.contains("setPublicId"), "has method setPublicId");
        check(mn.contains("setSystemId"), "has method setSystemId");
        check(mn.contains("writeAsEncodedUnicodeEx"), "has method writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
