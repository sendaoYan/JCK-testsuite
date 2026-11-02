/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.runtime.RuntimeTypeInfoSet conformance: interface hierarchy and members.
 * @run main RuntimeTypeInfoSetTest
 */

public class RuntimeTypeInfoSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.runtime.RuntimeTypeInfoSet", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.runtime.RuntimeTypeInfoSet"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.TypeInfoSet"), "has interface com.sun.xml.internal.bind.v2.model.core.TypeInfoSet");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("arrays"), "has method arrays");
        check(mn.contains("beans"), "has method beans");
        check(mn.contains("builtins"), "has method builtins");
        check(mn.contains("enums"), "has method enums");
        check(mn.contains("getAllElements"), "has method getAllElements");
        check(mn.contains("getAnyTypeInfo"), "has method getAnyTypeInfo");
        check(mn.contains("getClassInfo"), "has method getClassInfo");
        check(mn.contains("getElementInfo"), "has method getElementInfo");
        check(mn.contains("getElementMappings"), "has method getElementMappings");
        check(mn.contains("getTypeInfo"), "has method getTypeInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
