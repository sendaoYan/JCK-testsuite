/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.util.FlattenIterator conformance: class hierarchy and members.
 * @run main FlattenIteratorTest
 */

public class FlattenIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.util.FlattenIterator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.util.FlattenIterator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("java.util.Iterator"), "has interface java.util.Iterator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNext"), "has method getNext");
        check(mn.contains("hasNext"), "has method hasNext");
        check(mn.contains("next"), "has method next");
        check(mn.contains("remove"), "has method remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
