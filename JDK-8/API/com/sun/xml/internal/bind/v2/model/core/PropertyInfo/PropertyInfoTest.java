/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.core.PropertyInfo conformance: interface hierarchy and members.
 * @run main PropertyInfoTest
 */

public class PropertyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.core.PropertyInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.core.PropertyInfo"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.annotation.AnnotationSource"), "has interface com.sun.xml.internal.bind.v2.model.annotation.AnnotationSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("displayName"), "has method displayName");
        check(mn.contains("getAdapter"), "has method getAdapter");
        check(mn.contains("getExpectedMimeType"), "has method getExpectedMimeType");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getSchemaType"), "has method getSchemaType");
        check(mn.contains("id"), "has method id");
        check(mn.contains("inlineBinaryData"), "has method inlineBinaryData");
        check(mn.contains("isCollection"), "has method isCollection");
        check(mn.contains("kind"), "has method kind");
        check(mn.contains("parent"), "has method parent");
        check(mn.contains("ref"), "has method ref");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
