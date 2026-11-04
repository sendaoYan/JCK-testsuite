/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.DTMAxisIterator conformance: internal interface reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.dtm:+open
 * @run main DTMAxisIteratorTest
 */
public class DTMAxisIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.DTMAxisIterator", false, DTMAxisIteratorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.dtm.DTMAxisIterator"), "binary name");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 12, "public/protected method count 12");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 1, "public/protected field count 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
