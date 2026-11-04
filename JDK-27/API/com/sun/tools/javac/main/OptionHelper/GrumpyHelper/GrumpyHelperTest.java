/*
 * @test
 * @summary com.sun.tools.javac.main.OptionHelper.GrumpyHelper conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.main:+open
 * @run main GrumpyHelperTest
 */
public class GrumpyHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.main.OptionHelper$GrumpyHelper", false, GrumpyHelperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.main.OptionHelper$GrumpyHelper"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.main.OptionHelper"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
