/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.identity.Field.Matcher conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.xs.identity:+open
 * @run main MatcherTest
 */
public class MatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.identity.Field$Matcher", false, MatcherTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.xs.identity.Field$Matcher"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.identity.XPathMatcher"), "superclass com.sun.org.apache.xerces.internal.impl.xs.identity.XPathMatcher");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 2, "public/protected method count 2");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 2, "public/protected field count 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
