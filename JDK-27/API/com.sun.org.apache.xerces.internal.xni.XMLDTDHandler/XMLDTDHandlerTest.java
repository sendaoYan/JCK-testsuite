/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xni.XMLDTDHandler conformance: internal interface reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.xni:+open
 * @run main XMLDTDHandlerTest
 */
public class XMLDTDHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xni.XMLDTDHandler", false, XMLDTDHandlerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "binary name");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 22, "public/protected method count 22");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 2, "public/protected field count 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
