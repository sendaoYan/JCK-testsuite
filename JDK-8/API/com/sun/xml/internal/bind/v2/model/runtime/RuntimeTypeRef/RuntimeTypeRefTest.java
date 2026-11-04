/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.runtime.RuntimeTypeRef conformance: interface hierarchy and members.
 * @run main RuntimeTypeRefTest
 */

public class RuntimeTypeRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.runtime.RuntimeTypeRef", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.runtime.RuntimeTypeRef"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.TypeRef"), "has interface com.sun.xml.internal.bind.v2.model.core.TypeRef");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.runtime.RuntimeNonElementRef"), "has interface com.sun.xml.internal.bind.v2.model.runtime.RuntimeNonElementRef");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSource"), "has method getSource");
        check(mn.contains("getTarget"), "has method getTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
