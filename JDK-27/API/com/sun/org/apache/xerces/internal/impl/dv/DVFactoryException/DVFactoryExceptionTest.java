/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.DVFactoryException conformance: internal exception hierarchy and construction.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl.dv:+open
 * @run main DVFactoryExceptionTest
 */
public class DVFactoryExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.DVFactoryException", false, DVFactoryExceptionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.dv.DVFactoryException"), "binary name");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 0, "public/protected method count 0");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 0, "public/protected field count 0");
        java.lang.Throwable x = (java.lang.Throwable) c.getDeclaredConstructor().newInstance();
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
