/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.dtm.ref.sax2dtm:+open
 * @run main SAX2DTMTest
 */
public class SAX2DTMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM", false, SAX2DTMTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators");
        check(c.getInterfaces().length == 6, "interface count 6");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "interface org.xml.sax.ErrorHandler");
        check(hasInterface(c, "org.xml.sax.ext.DeclHandler"), "interface org.xml.sax.ext.DeclHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "interface org.xml.sax.ext.LexicalHandler");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 72, "public/protected method count 72");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 20, "public/protected field count 20");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
