/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.core.PropertyKind conformance: enum structure and members.
 * @run main PropertyKindTest
 */

public class PropertyKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.core.PropertyKind", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.core.PropertyKind"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(c.isEnum(), "is enum");
        check("java.lang.Enum".equals(c.getSuperclass().getName()), "extends java.lang.Enum");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("valueOf"), "has method valueOf");
        check(mn.contains("values"), "has method values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
