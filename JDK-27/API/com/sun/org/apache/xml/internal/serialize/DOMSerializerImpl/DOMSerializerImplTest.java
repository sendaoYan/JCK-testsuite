/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.serialize:+open
 * @run main DOMSerializerImplTest
 */
public class DOMSerializerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl", false, DOMSerializerImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interface count 2");
        check(hasInterface(c, "org.w3c.dom.DOMConfiguration"), "interface org.w3c.dom.DOMConfiguration");
        check(hasInterface(c, "org.w3c.dom.ls.LSSerializer"), "interface org.w3c.dom.ls.LSSerializer");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 12, "public/protected method count 12");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 13, "public/protected field count 13");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
