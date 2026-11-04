/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo conformance: interface hierarchy and members.
 * @run main RuntimeClassInfoTest
 */

public class RuntimeClassInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.runtime.RuntimeClassInfo"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.ClassInfo"), "has interface com.sun.xml.internal.bind.v2.model.core.ClassInfo");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.runtime.RuntimeNonElement"), "has interface com.sun.xml.internal.bind.v2.model.runtime.RuntimeNonElement");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttributeWildcard"), "has method getAttributeWildcard");
        check(mn.contains("getBaseClass"), "has method getBaseClass");
        check(mn.contains("getFactoryMethod"), "has method getFactoryMethod");
        check(mn.contains("getLocatorField"), "has method getLocatorField");
        check(mn.contains("getProperties"), "has method getProperties");
        check(mn.contains("getProperty"), "has method getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
