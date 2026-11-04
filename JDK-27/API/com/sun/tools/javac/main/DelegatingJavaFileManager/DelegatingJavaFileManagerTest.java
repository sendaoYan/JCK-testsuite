/*
 * @test
 * @summary com.sun.tools.javac.main.DelegatingJavaFileManager conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.main:+open
 * @run main DelegatingJavaFileManagerTest
 */
public class DelegatingJavaFileManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.main.DelegatingJavaFileManager", false, DelegatingJavaFileManagerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.main.DelegatingJavaFileManager"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "javax.tools.JavaFileManager"), "interface javax.tools.JavaFileManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
