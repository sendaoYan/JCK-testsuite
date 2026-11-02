/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.dtd:+open
 * @run main XMLDTDProcessorTest
 */
public class XMLDTDProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor", false, XMLDTDProcessorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interface count 3");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelFilter"), "interface com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelFilter");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter"), "interface com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 49, "public/protected method count 49");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 25, "public/protected field count 25");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
