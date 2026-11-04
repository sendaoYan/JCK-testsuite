/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.core.TypeInfoSet conformance: interface hierarchy and members.
 * @run main TypeInfoSetTest
 */

public class TypeInfoSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.core.TypeInfoSet", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.core.TypeInfoSet"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("arrays"), "has method arrays");
        check(mn.contains("beans"), "has method beans");
        check(mn.contains("builtins"), "has method builtins");
        check(mn.contains("dump"), "has method dump");
        check(mn.contains("enums"), "has method enums");
        check(mn.contains("getAllElements"), "has method getAllElements");
        check(mn.contains("getAnyTypeInfo"), "has method getAnyTypeInfo");
        check(mn.contains("getAttributeFormDefault"), "has method getAttributeFormDefault");
        check(mn.contains("getClassInfo"), "has method getClassInfo");
        check(mn.contains("getElementFormDefault"), "has method getElementFormDefault");
        check(mn.contains("getElementInfo"), "has method getElementInfo");
        check(mn.contains("getElementMappings"), "has method getElementMappings");
        check(mn.contains("getNavigator"), "has method getNavigator");
        check(mn.contains("getSchemaLocations"), "has method getSchemaLocations");
        check(mn.contains("getTypeInfo"), "has method getTypeInfo");
        check(mn.contains("getXmlNs"), "has method getXmlNs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
