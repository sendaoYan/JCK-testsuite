/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.Status conformance: internal enum reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.util:+open
 * @run main StatusTest
 */
public class StatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.Status", false, StatusTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.util.Status"), "binary name");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "enum is not interface");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 4, "public/protected method count 4");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 6, "public/protected field count 6");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 6, "enum constant count 6");
        check(ec.contains("NOT_ALLOWED"), "enum constant NOT_ALLOWED");
        check(ec.contains("NOT_RECOGNIZED"), "enum constant NOT_RECOGNIZED");
        check(ec.contains("NOT_SUPPORTED"), "enum constant NOT_SUPPORTED");
        check(ec.contains("RECOGNIZED"), "enum constant RECOGNIZED");
        check(ec.contains("SET"), "enum constant SET");
        check(ec.contains("UNKNOWN"), "enum constant UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
