/*
 * @test
 * @summary com.sun.tools.javac.code.Type conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main TypeTest
 */
public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Type", false, TypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Type"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.code.AnnoConstruct"), "superclass");
        check(c.getInterfaces().length == 2, "interfaces count");
        check(hasInterface(c, "javax.lang.model.type.TypeMirror"), "interface javax.lang.model.type.TypeMirror");
        check(hasInterface(c, "com.sun.tools.javac.jvm.PoolConstant"), "interface com.sun.tools.javac.jvm.PoolConstant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
