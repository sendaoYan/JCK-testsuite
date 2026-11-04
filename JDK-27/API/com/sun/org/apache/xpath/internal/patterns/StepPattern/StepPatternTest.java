/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.patterns.StepPattern conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xpath.internal.patterns:+open
 * @run main StepPatternTest
 */
public class StepPatternTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.patterns.StepPattern", false, StepPatternTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xpath.internal.patterns.StepPattern"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.patterns.NodeTest"), "superclass com.sun.org.apache.xpath.internal.patterns.NodeTest");
        check(c.getInterfaces().length == 2, "interface count 2");
        check(hasInterface(c, "com.sun.org.apache.xpath.internal.ExpressionOwner"), "interface com.sun.org.apache.xpath.internal.ExpressionOwner");
        check(hasInterface(c, "com.sun.org.apache.xpath.internal.axes.SubContextList"), "interface com.sun.org.apache.xpath.internal.axes.SubContextList");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 27, "public/protected method count 27");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 1, "public/protected field count 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
