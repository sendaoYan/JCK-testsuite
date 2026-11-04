/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl:+open
 * @run main XMLDocumentFragmentScannerImplTest
 */
public class XMLDocumentFragmentScannerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl", false, XMLDocumentFragmentScannerImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.XMLScanner"), "superclass com.sun.org.apache.xerces.internal.impl.XMLScanner");
        check(c.getInterfaces().length == 4, "interface count 4");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.XMLEntityHandler"), "interface com.sun.org.apache.xerces.internal.impl.XMLEntityHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner"), "interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner");
        check(hasInterface(c, "com.sun.xml.internal.stream.XMLBufferListener"), "interface com.sun.xml.internal.stream.XMLBufferListener");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 45, "public/protected method count 45");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 86, "public/protected field count 86");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
