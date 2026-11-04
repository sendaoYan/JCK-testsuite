/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack conformance: class hierarchy and members.
 * @run main PackTest
 */

public class PackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.reflect.Lister$Pack", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.reflect.Lister$Pack"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.util.ArrayList".equals(c.getSuperclass().getName()), "extends java.util.ArrayList");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("build"), "has method build");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
