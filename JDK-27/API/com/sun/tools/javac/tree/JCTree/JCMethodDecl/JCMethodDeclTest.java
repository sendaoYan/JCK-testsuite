/*
 * @test
 * @summary com.sun.tools.javac.tree.JCTree.JCMethodDecl conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main JCMethodDeclTest
 */
public class JCMethodDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.JCTree$JCMethodDecl", false, JCMethodDeclTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.JCTree$JCMethodDecl"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.JCTree"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.tree.MethodTree"), "interface com.sun.source.tree.MethodTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
