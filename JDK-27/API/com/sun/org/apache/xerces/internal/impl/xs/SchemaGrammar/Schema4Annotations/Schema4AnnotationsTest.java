/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar.Schema4Annotations conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.xs:+open
 * @run main Schema4AnnotationsTest
 */
public class Schema4AnnotationsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar$Schema4Annotations", false, Schema4AnnotationsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar$Schema4Annotations"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar"), "superclass com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 22, "public/protected method count 22");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 1, "public/protected field count 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
