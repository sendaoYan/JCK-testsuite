/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.NodeSetDTM conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xpath.internal:+open
 * @run main NodeSetDTMTest
 */
public class NodeSetDTMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.NodeSetDTM", false, NodeSetDTMTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xpath.internal.NodeSetDTM"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.utils.NodeVector"), "superclass com.sun.org.apache.xml.internal.utils.NodeVector");
        check(c.getInterfaces().length == 2, "interface count 2");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.dtm.DTMIterator"), "interface com.sun.org.apache.xml.internal.dtm.DTMIterator");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 49, "public/protected method count 49");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 3, "public/protected field count 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
