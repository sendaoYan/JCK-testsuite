/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.schemagen.xmlschema.Schema conformance: interface hierarchy and members.
 * @run main SchemaTest
 */

public class SchemaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.schemagen.xmlschema.Schema", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.schemagen.xmlschema.Schema"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.schemagen.xmlschema.SchemaTop"), "has interface com.sun.xml.internal.bind.v2.schemagen.xmlschema.SchemaTop");
        check(ifs.contains("com.sun.xml.internal.txw2.TypedXmlWriter"), "has interface com.sun.xml.internal.txw2.TypedXmlWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_import"), "has method _import");
        check(mn.contains("annotation"), "has method annotation");
        check(mn.contains("attributeFormDefault"), "has method attributeFormDefault");
        check(mn.contains("blockDefault"), "has method blockDefault");
        check(mn.contains("elementFormDefault"), "has method elementFormDefault");
        check(mn.contains("finalDefault"), "has method finalDefault");
        check(mn.contains("id"), "has method id");
        check(mn.contains("lang"), "has method lang");
        check(mn.contains("targetNamespace"), "has method targetNamespace");
        check(mn.contains("version"), "has method version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
