/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.schemagen.xmlschema.Redefinable conformance: interface hierarchy and members.
 * @run main RedefinableTest
 */

public class RedefinableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.schemagen.xmlschema.Redefinable", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.schemagen.xmlschema.Redefinable"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexTypeHost"), "has interface com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexTypeHost");
        check(ifs.contains("com.sun.xml.internal.bind.v2.schemagen.xmlschema.SimpleTypeHost"), "has interface com.sun.xml.internal.bind.v2.schemagen.xmlschema.SimpleTypeHost");
        check(ifs.contains("com.sun.xml.internal.txw2.TypedXmlWriter"), "has interface com.sun.xml.internal.txw2.TypedXmlWriter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
