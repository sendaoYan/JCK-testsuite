/*
 * @test
 * @summary com.sun.tools.javac.jvm.PoolConstant.LoadableConstant conformance: internal interface reflection.
 * @modules jdk.compiler/com.sun.tools.javac.jvm:+open
 * @run main LoadableConstantTest
 */
public class LoadableConstantTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.jvm.PoolConstant$LoadableConstant", false, LoadableConstantTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.jvm.PoolConstant$LoadableConstant"), "name");
        check(c.isInterface(), "interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.jvm.PoolConstant"), "interface com.sun.tools.javac.jvm.PoolConstant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
