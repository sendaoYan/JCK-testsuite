/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.LocPathIterator conformance: internal abstract class reflection.
 * @modules java.xml/com.sun.org.apache.xpath.internal.axes:+open
 * @run main LocPathIteratorTest
 */
public class LocPathIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.LocPathIterator", false, LocPathIteratorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xpath.internal.axes.LocPathIterator"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest"), "superclass com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest");
        check(c.getInterfaces().length == 4, "interface count 4");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.dtm.DTMIterator"), "interface com.sun.org.apache.xml.internal.dtm.DTMIterator");
        check(hasInterface(c, "com.sun.org.apache.xpath.internal.axes.PathComponent"), "interface com.sun.org.apache.xpath.internal.axes.PathComponent");
        check(hasInterface(c, "java.io.Serializable"), "interface java.io.Serializable");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 47, "public/protected method count 47");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 9, "public/protected field count 9");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
