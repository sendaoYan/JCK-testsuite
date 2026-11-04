/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.util.QNameMap conformance: class hierarchy and members.
 * @run main QNameMapTest
 */

public class QNameMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.util.QNameMap", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.util.QNameMap"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addEntry"), "has method addEntry");
        check(mn.contains("containsKey"), "has method containsKey");
        check(mn.contains("entrySet"), "has method entrySet");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getEntry"), "has method getEntry");
        check(mn.contains("getOne"), "has method getOne");
        check(mn.contains("hash"), "has method hash");
        check(mn.contains("indexFor"), "has method indexFor");
        check(mn.contains("isEmpty"), "has method isEmpty");
        check(mn.contains("keySet"), "has method keySet");
        check(mn.contains("newEntryIterator"), "has method newEntryIterator");
        check(mn.contains("put"), "has method put");
        check(mn.contains("putAll"), "has method putAll");
        check(mn.contains("resize"), "has method resize");
        check(mn.contains("size"), "has method size");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("transfer"), "has method transfer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
