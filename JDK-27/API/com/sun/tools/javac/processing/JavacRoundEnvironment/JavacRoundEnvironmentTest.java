/*
 * @test
 * @summary com.sun.tools.javac.processing.JavacRoundEnvironment conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.processing:+open
 * @run main JavacRoundEnvironmentTest
 */
public class JavacRoundEnvironmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.processing.JavacRoundEnvironment", false, JavacRoundEnvironmentTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.processing.JavacRoundEnvironment"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "javax.annotation.processing.RoundEnvironment"), "interface javax.annotation.processing.RoundEnvironment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
