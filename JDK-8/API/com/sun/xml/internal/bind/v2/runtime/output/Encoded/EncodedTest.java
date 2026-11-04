/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.output.Encoded conformance: class hierarchy and members.
 * @run main EncodedTest
 */

public class EncodedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.output.Encoded", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.output.Encoded"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "has method add");
        check(mn.contains("append"), "has method append");
        check(mn.contains("compact"), "has method compact");
        check(mn.contains("ensureSize"), "has method ensureSize");
        check(mn.contains("set"), "has method set");
        check(mn.contains("setEscape"), "has method setEscape");
        check(mn.contains("write"), "has method write");
        check(mn.contains("writeEntity"), "has method writeEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
