/*
 * @test
 * @summary com.sun.tools.javac.parser.JavaTokenizer.BasicComment conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.parser:+open
 * @run main BasicCommentTest
 */
public class BasicCommentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.parser.JavaTokenizer$BasicComment", false, BasicCommentTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.parser.JavaTokenizer$BasicComment"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.parser.UnicodeReader$PositionTrackingReader"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.parser.Tokens$Comment"), "interface com.sun.tools.javac.parser.Tokens$Comment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
