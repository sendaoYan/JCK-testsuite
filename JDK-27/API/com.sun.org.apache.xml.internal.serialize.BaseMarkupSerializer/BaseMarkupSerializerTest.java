/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer conformance: internal abstract class reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.serialize:+open
 * @run main BaseMarkupSerializerTest
 */
public class BaseMarkupSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer", false, BaseMarkupSerializerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 7, "interface count 7");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serialize.DOMSerializer"), "interface com.sun.org.apache.xml.internal.serialize.DOMSerializer");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.serialize.Serializer"), "interface com.sun.org.apache.xml.internal.serialize.Serializer");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "interface org.xml.sax.DocumentHandler");
        check(hasInterface(c, "org.xml.sax.ext.DeclHandler"), "interface org.xml.sax.ext.DeclHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "interface org.xml.sax.ext.LexicalHandler");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 64, "public/protected method count 64");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 14, "public/protected field count 14");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
