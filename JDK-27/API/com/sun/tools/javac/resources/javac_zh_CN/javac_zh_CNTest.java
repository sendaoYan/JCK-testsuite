/*
 * @test
 * @summary com.sun.tools.javac.resources.javac_zh_CN conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.resources:+open
 * @run main javac_zh_CNTest
 */
public class javac_zh_CNTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.resources.javac_zh_CN", false, javac_zh_CNTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.resources.javac_zh_CN"), "name");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
