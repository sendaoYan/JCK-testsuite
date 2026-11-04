/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.DOMNormalizer.XMLAttributesProxy conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.dom:+open
 * @run main XMLAttributesProxyTest
 */
public class XMLAttributesProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.DOMNormalizer$XMLAttributesProxy", false, XMLAttributesProxyTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.dom.DOMNormalizer$XMLAttributesProxy"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLAttributes"), "interface com.sun.org.apache.xerces.internal.xni.XMLAttributes");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 31, "public/protected method count 31");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 5, "public/protected field count 5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
