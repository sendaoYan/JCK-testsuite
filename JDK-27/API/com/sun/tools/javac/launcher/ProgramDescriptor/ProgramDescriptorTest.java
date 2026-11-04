/*
 * @test
 * @summary com.sun.tools.javac.launcher.ProgramDescriptor conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.launcher:+open
 * @run main ProgramDescriptorTest
 */
public class ProgramDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.launcher.ProgramDescriptor", false, ProgramDescriptorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.launcher.ProgramDescriptor"), "name");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
