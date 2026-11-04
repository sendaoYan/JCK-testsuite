/*
 * @test
 * @summary com.sun.tools.javac.file.BaseFileManager conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.file:+open
 * @run main BaseFileManagerTest
 */
public class BaseFileManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.file.BaseFileManager", false, BaseFileManagerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.file.BaseFileManager"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "javax.tools.JavaFileManager"), "interface javax.tools.JavaFileManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
