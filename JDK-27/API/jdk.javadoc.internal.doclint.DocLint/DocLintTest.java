/*
 * @test
 * @summary jdk.javadoc.internal.doclint.DocLint conformance: internal class reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclint:+open
 * @run main DocLintTest
 */
public class DocLintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclint.DocLint", false, DocLintTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.doclint.DocLint"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 21, "21 declared methods");
        check(c.getDeclaredFields().length == 14, "14 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
