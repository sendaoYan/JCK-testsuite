/*
 * @test
 * @summary jdk.jpackage.internal.util.PListReader conformance: internal final class reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.util:+open
 * @run main PListReaderTest
 */
public class PListReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.util.PListReader", false, PListReaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 29, "29 declared methods");
        check(c.getDeclaredFields().length == 1, "1 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
