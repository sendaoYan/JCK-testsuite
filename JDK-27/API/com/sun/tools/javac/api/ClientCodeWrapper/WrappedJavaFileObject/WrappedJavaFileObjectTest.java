/*
 * @test
 * @summary com.sun.tools.javac.api.ClientCodeWrapper.WrappedJavaFileObject conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.api:+open
 * @run main WrappedJavaFileObjectTest
 */
public class WrappedJavaFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.api.ClientCodeWrapper$WrappedJavaFileObject", false, WrappedJavaFileObjectTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.api.ClientCodeWrapper$WrappedJavaFileObject"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.api.ClientCodeWrapper$WrappedFileObject"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "javax.tools.JavaFileObject"), "interface javax.tools.JavaFileObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
