/*
 * @test
 * @summary com.sun.org.apache.xml.internal.res.XMLErrorResources_zh_CN conformance: internal class reflection.
 * @modules java.xml/com.sun.org.apache.xml.internal.res:+open
 * @run main XMLErrorResources_zh_CNTest
 */
public class XMLErrorResources_zh_CNTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.res.XMLErrorResources_zh_CN", false, XMLErrorResources_zh_CNTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.res.XMLErrorResources_zh_CN"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass java.util.ListResourceBundle");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 1, "public/protected method count 1");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 79, "public/protected field count 79");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
