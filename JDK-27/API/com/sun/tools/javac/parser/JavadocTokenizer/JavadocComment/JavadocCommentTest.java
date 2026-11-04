/*
 * @test
 * @summary com.sun.tools.javac.parser.JavadocTokenizer.JavadocComment conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.parser:+open
 * @run main JavadocCommentTest
 */
public class JavadocCommentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.parser.JavadocTokenizer$JavadocComment", false, JavadocCommentTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.parser.JavadocTokenizer$JavadocComment"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.parser.JavaTokenizer$BasicComment"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
