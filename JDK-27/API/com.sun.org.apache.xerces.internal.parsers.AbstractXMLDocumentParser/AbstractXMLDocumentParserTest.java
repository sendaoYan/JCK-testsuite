/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser conformance: internal abstract class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.parsers:+open
 * @run main AbstractXMLDocumentParserTest
 */
public class AbstractXMLDocumentParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser", false, AbstractXMLDocumentParserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.XMLParser"), "superclass com.sun.org.apache.xerces.internal.parsers.XMLParser");
        check(c.getInterfaces().length == 3, "interface count 3");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDTDHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 50, "public/protected method count 50");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 4, "public/protected field count 4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
