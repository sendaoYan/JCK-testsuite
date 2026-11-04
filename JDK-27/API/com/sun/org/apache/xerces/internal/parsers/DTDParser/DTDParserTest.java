/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.DTDParser conformance: internal abstract class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.parsers:+open
 * @run main DTDParserTest
 */
public class DTDParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.DTDParser", false, DTDParserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.parsers.DTDParser"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.XMLGrammarParser"), "superclass com.sun.org.apache.xerces.internal.parsers.XMLGrammarParser");
        check(c.getInterfaces().length == 2, "interface count 2");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLDTDHandler"), "interface com.sun.org.apache.xerces.internal.xni.XMLDTDHandler");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 28, "public/protected method count 28");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 1, "public/protected field count 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
