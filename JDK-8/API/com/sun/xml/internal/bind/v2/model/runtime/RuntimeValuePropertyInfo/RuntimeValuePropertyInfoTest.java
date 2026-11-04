/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.runtime.RuntimeValuePropertyInfo conformance: interface hierarchy and members.
 * @run main RuntimeValuePropertyInfoTest
 */

public class RuntimeValuePropertyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.runtime.RuntimeValuePropertyInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.runtime.RuntimeValuePropertyInfo"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.ValuePropertyInfo"), "has interface com.sun.xml.internal.bind.v2.model.core.ValuePropertyInfo");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.runtime.RuntimeNonElementRef"), "has interface com.sun.xml.internal.bind.v2.model.runtime.RuntimeNonElementRef");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.runtime.RuntimePropertyInfo"), "has interface com.sun.xml.internal.bind.v2.model.runtime.RuntimePropertyInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getTarget"), "has method getTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
