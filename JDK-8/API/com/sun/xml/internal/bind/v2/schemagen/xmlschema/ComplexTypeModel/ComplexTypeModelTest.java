/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexTypeModel conformance: interface hierarchy and members.
 * @run main ComplexTypeModelTest
 */

public class ComplexTypeModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexTypeModel", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.schemagen.xmlschema.ComplexTypeModel"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.schemagen.xmlschema.AttrDecls"), "has interface com.sun.xml.internal.bind.v2.schemagen.xmlschema.AttrDecls");
        check(ifs.contains("com.sun.xml.internal.bind.v2.schemagen.xmlschema.TypeDefParticle"), "has interface com.sun.xml.internal.bind.v2.schemagen.xmlschema.TypeDefParticle");
        check(ifs.contains("com.sun.xml.internal.txw2.TypedXmlWriter"), "has interface com.sun.xml.internal.txw2.TypedXmlWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("complexContent"), "has method complexContent");
        check(mn.contains("mixed"), "has method mixed");
        check(mn.contains("simpleContent"), "has method simpleContent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
