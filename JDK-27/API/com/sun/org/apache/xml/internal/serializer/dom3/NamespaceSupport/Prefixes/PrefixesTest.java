/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.dom3.NamespaceSupport.Prefixes conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.serializer.dom3:+open
 * @run main PrefixesTest
 */
public class PrefixesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.dom3.NamespaceSupport$Prefixes", false, PrefixesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.serializer.dom3.NamespaceSupport$Prefixes"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(hasInterface(c, "java.util.Enumeration"), "interface java.util.Enumeration");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 4, "public/protected method count 4");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
