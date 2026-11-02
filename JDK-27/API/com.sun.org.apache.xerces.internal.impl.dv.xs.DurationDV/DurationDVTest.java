/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.DurationDV conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.dv.xs:+open
 * @run main DurationDVTest
 */
public class DurationDVTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.DurationDV", false, DurationDVTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.dv.xs.DurationDV"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV"), "superclass com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 6, "public/protected method count 6");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 3, "public/protected field count 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
