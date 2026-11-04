/*
 * @test
 * @summary com.sun.tools.javac.processing.JavacFiler conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.processing:+open
 * @run main JavacFilerTest
 */
public class JavacFilerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.processing.JavacFiler", false, JavacFilerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.processing.JavacFiler"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 2, "interfaces count");
        check(hasInterface(c, "javax.annotation.processing.Filer"), "interface javax.annotation.processing.Filer");
        check(hasInterface(c, "java.io.Closeable"), "interface java.io.Closeable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
