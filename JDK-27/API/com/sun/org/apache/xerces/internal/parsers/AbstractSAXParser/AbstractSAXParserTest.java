/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser conformance: internal abstract class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.parsers:+open
 * @run main AbstractSAXParserTest
 */
public class AbstractSAXParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser", false, AbstractSAXParserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser"), "superclass com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser");
        check(c.getInterfaces().length == 3, "interface count 3");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.PSVIProvider"), "interface com.sun.org.apache.xerces.internal.xs.PSVIProvider");
        check(hasInterface(c, "org.xml.sax.Parser"), "interface org.xml.sax.Parser");
        check(hasInterface(c, "org.xml.sax.XMLReader"), "interface org.xml.sax.XMLReader");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 51, "public/protected method count 51");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 24, "public/protected field count 24");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
