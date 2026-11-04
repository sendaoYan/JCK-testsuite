/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.schemagen.xmlschema.TopLevelElement conformance: interface hierarchy and members.
 * @run main TopLevelElementTest
 */

public class TopLevelElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.schemagen.xmlschema.TopLevelElement", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.schemagen.xmlschema.TopLevelElement"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.schemagen.xmlschema.Element"), "has interface com.sun.xml.internal.bind.v2.schemagen.xmlschema.Element");
        check(ifs.contains("com.sun.xml.internal.txw2.TypedXmlWriter"), "has interface com.sun.xml.internal.txw2.TypedXmlWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_abstract"), "has method _abstract");
        check(mn.contains("_final"), "has method _final");
        check(mn.contains("name"), "has method name");
        check(mn.contains("substitutionGroup"), "has method substitutionGroup");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
