/*
 * @test
 * @summary com.sun.tools.javac.file.RelativePath.RelativeDirectory conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.file:+open
 * @run main RelativeDirectoryTest
 */
public class RelativeDirectoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.file.RelativePath$RelativeDirectory", false, RelativeDirectoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.file.RelativePath$RelativeDirectory"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.file.RelativePath"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
