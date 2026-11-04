/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.AttributesProxy conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.util:+open
 * @run main AttributesProxyTest
 */
public class AttributesProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.AttributesProxy", false, AttributesProxyTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.util.AttributesProxy"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interface count 2");
        check(hasInterface(c, "org.xml.sax.AttributeList"), "interface org.xml.sax.AttributeList");
        check(hasInterface(c, "org.xml.sax.ext.Attributes2"), "interface org.xml.sax.ext.Attributes2");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 21, "public/protected method count 21");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
