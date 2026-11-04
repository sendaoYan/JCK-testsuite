/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.schemagen.xmlschema.ContentModelContainer conformance: interface hierarchy and members.
 * @run main ContentModelContainerTest
 */

public class ContentModelContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.schemagen.xmlschema.ContentModelContainer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.schemagen.xmlschema.ContentModelContainer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.txw2.TypedXmlWriter"), "has interface com.sun.xml.internal.txw2.TypedXmlWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("all"), "has method all");
        check(mn.contains("any"), "has method any");
        check(mn.contains("choice"), "has method choice");
        check(mn.contains("element"), "has method element");
        check(mn.contains("sequence"), "has method sequence");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
