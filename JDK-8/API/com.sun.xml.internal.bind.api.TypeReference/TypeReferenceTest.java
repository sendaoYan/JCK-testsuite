/*
 * @test
 * @summary com.sun.xml.internal.bind.api.TypeReference conformance: class hierarchy and members.
 * @run main TypeReferenceTest
 */

public class TypeReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.api.TypeReference", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.api.TypeReference"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("equals"), "has method equals");
        check(mn.contains("get"), "has method get");
        check(mn.contains("hashCode"), "has method hashCode");
        check(mn.contains("toItemType"), "has method toItemType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
