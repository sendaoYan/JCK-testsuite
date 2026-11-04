/*
 * @test
 * @summary com.sun.tools.javac.code.Types.UnaryVisitor conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main UnaryVisitorTest
 */
public class UnaryVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Types$UnaryVisitor", false, UnaryVisitorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Types$UnaryVisitor"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.code.Types$SimpleVisitor"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
