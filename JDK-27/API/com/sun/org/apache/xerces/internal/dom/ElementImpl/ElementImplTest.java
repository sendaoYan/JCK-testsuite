/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.ElementImpl conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.dom:+open
 * @run main ElementImplTest
 */
public class ElementImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.ElementImpl", false, ElementImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.dom.ElementImpl"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ParentNode"), "superclass com.sun.org.apache.xerces.internal.dom.ParentNode");
        check(c.getInterfaces().length == 3, "interface count 3");
        check(hasInterface(c, "org.w3c.dom.Element"), "interface org.w3c.dom.Element");
        check(hasInterface(c, "org.w3c.dom.ElementTraversal"), "interface org.w3c.dom.ElementTraversal");
        check(hasInterface(c, "org.w3c.dom.TypeInfo"), "interface org.w3c.dom.TypeInfo");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 45, "public/protected method count 45");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 2, "public/protected field count 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
