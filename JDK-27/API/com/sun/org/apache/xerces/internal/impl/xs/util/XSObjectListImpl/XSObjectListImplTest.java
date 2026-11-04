/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.xs.util:+open
 * @run main XSObjectListImplTest
 */
public class XSObjectListImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl", false, XSObjectListImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractList"), "superclass java.util.AbstractList");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSObjectList"), "interface com.sun.org.apache.xerces.internal.xs.XSObjectList");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 14, "public/protected method count 14");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 1, "public/protected field count 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
