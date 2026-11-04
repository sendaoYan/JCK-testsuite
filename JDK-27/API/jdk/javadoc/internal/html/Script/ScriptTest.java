/*
 * @test
 * @summary jdk.javadoc.internal.html.Script conformance: internal class reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.html:+open
 * @run main ScriptTest
 */
public class ScriptTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.html.Script", false, ScriptTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 6, "6 declared methods");
        check(c.getDeclaredFields().length == 1, "1 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
