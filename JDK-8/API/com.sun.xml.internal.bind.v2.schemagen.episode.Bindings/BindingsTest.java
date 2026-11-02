/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.schemagen.episode.Bindings conformance: interface hierarchy and members.
 * @run main BindingsTest
 */

public class BindingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.schemagen.episode.Bindings", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.schemagen.episode.Bindings"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.txw2.TypedXmlWriter"), "has interface com.sun.xml.internal.txw2.TypedXmlWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("bindings"), "has method bindings");
        check(mn.contains("klass"), "has method klass");
        check(mn.contains("scd"), "has method scd");
        check(mn.contains("schemaBindings"), "has method schemaBindings");
        check(mn.contains("typesafeEnumClass"), "has method typesafeEnumClass");
        check(mn.contains("version"), "has method version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
