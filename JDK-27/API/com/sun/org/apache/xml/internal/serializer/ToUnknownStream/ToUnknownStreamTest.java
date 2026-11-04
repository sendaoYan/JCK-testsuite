/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.ToUnknownStream conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.serializer:+open
 * @run main ToUnknownStreamTest
 */
public class ToUnknownStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.ToUnknownStream", false, ToUnknownStreamTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.serializer.ToUnknownStream"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.SerializerBase"), "superclass com.sun.org.apache.xml.internal.serializer.SerializerBase");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 79, "public/protected method count 79");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
