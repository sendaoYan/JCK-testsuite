/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.util.QNameMap.Entry conformance: class hierarchy and members.
 * @run main EntryTest
 */

public class EntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.util.QNameMap$Entry", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.util.QNameMap$Entry"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createQName"), "has method createQName");
        check(mn.contains("equals"), "has method equals");
        check(mn.contains("getValue"), "has method getValue");
        check(mn.contains("hashCode"), "has method hashCode");
        check(mn.contains("setValue"), "has method setValue");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
