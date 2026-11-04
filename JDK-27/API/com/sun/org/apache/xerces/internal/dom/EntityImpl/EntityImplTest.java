/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.EntityImpl conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.dom:+open
 * @run main EntityImplTest
 */
public class EntityImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.EntityImpl", false, EntityImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.dom.EntityImpl"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ParentNode"), "superclass com.sun.org.apache.xerces.internal.dom.ParentNode");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(hasInterface(c, "org.w3c.dom.Entity"), "interface org.w3c.dom.Entity");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 19, "public/protected method count 19");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 8, "public/protected field count 8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
