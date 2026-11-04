/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.NameBuilder conformance: class hierarchy and members.
 * @run main NameBuilderTest
 */

public class NameBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.NameBuilder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.NameBuilder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("allocIndex"), "has method allocIndex");
        check(mn.contains("conclude"), "has method conclude");
        check(mn.contains("createAttributeName"), "has method createAttributeName");
        check(mn.contains("createElementName"), "has method createElementName");
        check(mn.contains("createName"), "has method createName");
        check(mn.contains("list"), "has method list");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
