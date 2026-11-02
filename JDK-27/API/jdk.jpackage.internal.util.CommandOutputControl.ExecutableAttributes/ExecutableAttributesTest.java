/*
 * @test
 * @summary jdk.jpackage.internal.util.CommandOutputControl.ExecutableAttributes conformance: internal interface reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.util:+open
 * @run main ExecutableAttributesTest
 */
public class ExecutableAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.util.CommandOutputControl$ExecutableAttributes", false, ExecutableAttributesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 2, "2 declared methods");
        check(c.getDeclaredFields().length == 0, "0 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
