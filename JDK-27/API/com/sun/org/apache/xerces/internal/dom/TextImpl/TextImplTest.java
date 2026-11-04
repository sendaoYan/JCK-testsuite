/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.TextImpl conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.dom:+open
 * @run main TextImplTest
 */
public class TextImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.TextImpl", false, TextImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.dom.TextImpl"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.CharacterDataImpl"), "superclass com.sun.org.apache.xerces.internal.dom.CharacterDataImpl");
        check(c.getInterfaces().length == 2, "interface count 2");
        check(hasInterface(c, "org.w3c.dom.CharacterData"), "interface org.w3c.dom.CharacterData");
        check(hasInterface(c, "org.w3c.dom.Text"), "interface org.w3c.dom.Text");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 12, "public/protected method count 12");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
