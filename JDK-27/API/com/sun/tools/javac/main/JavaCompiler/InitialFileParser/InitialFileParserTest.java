/*
 * @test
 * @summary com.sun.tools.javac.main.JavaCompiler.InitialFileParser conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.main:+open
 * @run main InitialFileParserTest
 */
public class InitialFileParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.main.JavaCompiler$InitialFileParser", false, InitialFileParserTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.main.JavaCompiler$InitialFileParser"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.main.JavaCompiler$InitialFileParserIntf"), "interface com.sun.tools.javac.main.JavaCompiler$InitialFileParserIntf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
