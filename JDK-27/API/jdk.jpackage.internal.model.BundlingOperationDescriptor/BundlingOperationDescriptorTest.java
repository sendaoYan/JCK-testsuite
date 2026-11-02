/*
 * @test
 * @summary jdk.jpackage.internal.model.BundlingOperationDescriptor conformance: internal final class reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.model:+open
 * @run main BundlingOperationDescriptorTest
 */
public class BundlingOperationDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.model.BundlingOperationDescriptor", false, BundlingOperationDescriptorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 7, "7 declared methods");
        check(c.getDeclaredFields().length == 4, "4 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
