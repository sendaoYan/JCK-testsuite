/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.dtm.ref:+open
 * @run main DTMNodeProxyTest
 */
public class DTMNodeProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy", false, DTMNodeProxyTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 8, "interface count 8");
        check(hasInterface(c, "org.w3c.dom.Attr"), "interface org.w3c.dom.Attr");
        check(hasInterface(c, "org.w3c.dom.Comment"), "interface org.w3c.dom.Comment");
        check(hasInterface(c, "org.w3c.dom.Document"), "interface org.w3c.dom.Document");
        check(hasInterface(c, "org.w3c.dom.DocumentFragment"), "interface org.w3c.dom.DocumentFragment");
        check(hasInterface(c, "org.w3c.dom.Element"), "interface org.w3c.dom.Element");
        check(hasInterface(c, "org.w3c.dom.Node"), "interface org.w3c.dom.Node");
        check(hasInterface(c, "org.w3c.dom.ProcessingInstruction"), "interface org.w3c.dom.ProcessingInstruction");
        check(hasInterface(c, "org.w3c.dom.Text"), "interface org.w3c.dom.Text");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 123, "public/protected method count 123");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 3, "public/protected field count 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
