/*
 * @test
 * @summary com.sun.xml.internal.stream.Entity.ExternalEntity conformance: class hierarchy and members.
 * @run main ExternalEntityTest
 */

public class ExternalEntityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.Entity$ExternalEntity", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.Entity$ExternalEntity"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.Entity".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.Entity");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("isExternal"), "has method isExternal");
        check(mn.contains("isUnparsed"), "has method isUnparsed");
        check(mn.contains("setValues"), "has method setValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
