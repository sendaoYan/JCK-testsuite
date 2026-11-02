/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.xs.opti:+open
 * @run main DefaultXMLDocumentHandlerTest
 */
public class DefaultXMLDocumentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler", false, DefaultXMLDocumentHandlerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interface count 3");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDTDHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 51, "public/protected method count 51");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
