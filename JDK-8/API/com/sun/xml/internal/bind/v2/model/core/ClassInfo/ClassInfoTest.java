/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.core.ClassInfo conformance: interface hierarchy and members.
 * @run main ClassInfoTest
 */

public class ClassInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.core.ClassInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.core.ClassInfo"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.MaybeElement"), "has interface com.sun.xml.internal.bind.v2.model.core.MaybeElement");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("declaresAttributeWildcard"), "has method declaresAttributeWildcard");
        check(mn.contains("getBaseClass"), "has method getBaseClass");
        check(mn.contains("getClazz"), "has method getClazz");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getProperties"), "has method getProperties");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("hasAttributeWildcard"), "has method hasAttributeWildcard");
        check(mn.contains("hasProperties"), "has method hasProperties");
        check(mn.contains("hasSubClasses"), "has method hasSubClasses");
        check(mn.contains("hasValueProperty"), "has method hasValueProperty");
        check(mn.contains("inheritsAttributeWildcard"), "has method inheritsAttributeWildcard");
        check(mn.contains("isAbstract"), "has method isAbstract");
        check(mn.contains("isFinal"), "has method isFinal");
        check(mn.contains("isOrdered"), "has method isOrdered");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
