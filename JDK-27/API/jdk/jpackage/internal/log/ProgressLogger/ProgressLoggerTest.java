/*
 * @test
 * @summary jdk.jpackage.internal.log.ProgressLogger conformance: internal interface reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.log:+open
 * @run main ProgressLoggerTest
 */
public class ProgressLoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.log.ProgressLogger", false, ProgressLoggerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 6, "6 declared methods");
        check(c.getDeclaredFields().length == 1, "1 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
