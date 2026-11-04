/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.Expression conformance: internal abstract class reflection.
 * @modules java.xml/com.sun.org.apache.xpath.internal:+open
 * @run main ExpressionTest
 */
public class ExpressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.Expression", false, ExpressionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xpath.internal.Expression"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interface count 3");
        check(hasInterface(c, "com.sun.org.apache.xpath.internal.ExpressionNode"), "interface com.sun.org.apache.xpath.internal.ExpressionNode");
        check(hasInterface(c, "com.sun.org.apache.xpath.internal.XPathVisitable"), "interface com.sun.org.apache.xpath.internal.XPathVisitable");
        check(hasInterface(c, "java.io.Serializable"), "interface java.io.Serializable");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 30, "public/protected method count 30");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
