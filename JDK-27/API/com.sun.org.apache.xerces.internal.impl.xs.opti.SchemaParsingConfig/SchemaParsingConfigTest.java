/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaParsingConfig conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.xs.opti:+open
 * @run main SchemaParsingConfigTest
 */
public class SchemaParsingConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaParsingConfig", false, SchemaParsingConfigTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaParsingConfig"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration"), "superclass com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration"), "interface com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 17, "public/protected method count 17");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 40, "public/protected field count 40");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
