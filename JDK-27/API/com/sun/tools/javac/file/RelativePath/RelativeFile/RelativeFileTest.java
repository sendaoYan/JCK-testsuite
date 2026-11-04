/*
 * @test
 * @summary com.sun.tools.javac.file.RelativePath.RelativeFile conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.file:+open
 * @run main RelativeFileTest
 */
public class RelativeFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.file.RelativePath$RelativeFile", false, RelativeFileTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.file.RelativePath$RelativeFile"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.file.RelativePath"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
