/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLEntityDescription conformance: internal interface reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl:+open
 * @run main XMLEntityDescriptionTest
 */
public class XMLEntityDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLEntityDescription", false, XMLEntityDescriptionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.XMLEntityDescription"), "binary name");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier"), "interface com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 2, "public/protected method count 2");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
