/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.FinalArrayList conformance: class hierarchy and members.
 * @run main FinalArrayListTest
 */

public class FinalArrayListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.FinalArrayList", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.FinalArrayList"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.util.ArrayList".equals(c.getSuperclass().getName()), "extends java.util.ArrayList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
