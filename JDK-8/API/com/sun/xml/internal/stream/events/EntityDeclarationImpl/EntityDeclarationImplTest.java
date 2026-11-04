/*
 * @test
 * @summary com.sun.xml.internal.stream.events.EntityDeclarationImpl conformance: class hierarchy and members.
 * @run main EntityDeclarationImplTest
 */

public class EntityDeclarationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.EntityDeclarationImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.EntityDeclarationImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.events.DummyEvent".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.events.DummyEvent");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.EntityDeclaration"), "has interface javax.xml.stream.events.EntityDeclaration");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBaseURI"), "has method getBaseURI");
        check(mn.contains("getEntityName"), "has method getEntityName");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getNotationName"), "has method getNotationName");
        check(mn.contains("getPublicId"), "has method getPublicId");
        check(mn.contains("getReplacementText"), "has method getReplacementText");
        check(mn.contains("getSystemId"), "has method getSystemId");
        check(mn.contains("getXMLResourceIdentifier"), "has method getXMLResourceIdentifier");
        check(mn.contains("init"), "has method init");
        check(mn.contains("setEntityName"), "has method setEntityName");
        check(mn.contains("setEntityReplacementText"), "has method setEntityReplacementText");
        check(mn.contains("setNotationName"), "has method setNotationName");
        check(mn.contains("setXMLResourceIdentifier"), "has method setXMLResourceIdentifier");
        check(mn.contains("writeAsEncodedUnicodeEx"), "has method writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
