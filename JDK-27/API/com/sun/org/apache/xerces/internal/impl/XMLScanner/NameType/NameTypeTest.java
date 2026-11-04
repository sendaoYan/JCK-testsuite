/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLScanner.NameType conformance: internal enum reflection.
 * @modules java.xml/com.sun.org.apache.xerces.internal.impl:+open
 * @run main NameTypeTest
 */
public class NameTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLScanner$NameType", false, NameTypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xerces.internal.impl.XMLScanner$NameType"), "binary name");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "enum is not interface");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        int mcount = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) { int mm = m.getModifiers(); if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) mcount++; }
        check(mcount == 2, "public/protected method count 2");
        int fcount = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { int fm = f.getModifiers(); if (java.lang.reflect.Modifier.isPublic(fm) || java.lang.reflect.Modifier.isProtected(fm)) fcount++; }
        check(fcount == 10, "public/protected field count 10");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 10, "enum constant count 10");
        check(ec.contains("ATTRIBUTE"), "enum constant ATTRIBUTE");
        check(ec.contains("ATTRIBUTENAME"), "enum constant ATTRIBUTENAME");
        check(ec.contains("COMMENT"), "enum constant COMMENT");
        check(ec.contains("DOCTYPE"), "enum constant DOCTYPE");
        check(ec.contains("ELEMENTEND"), "enum constant ELEMENTEND");
        check(ec.contains("ELEMENTSTART"), "enum constant ELEMENTSTART");
        check(ec.contains("ENTITY"), "enum constant ENTITY");
        check(ec.contains("NOTATION"), "enum constant NOTATION");
        check(ec.contains("PI"), "enum constant PI");
        check(ec.contains("REFERENCE"), "enum constant REFERENCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
