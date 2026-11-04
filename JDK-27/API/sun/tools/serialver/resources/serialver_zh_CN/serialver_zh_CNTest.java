/*
 * @test
 * @summary sun.tools.serialver.resources.serialver_zh_CN conformance: internal class reflection.
 * @modules jdk.compiler/sun.tools.serialver.resources:+open
 * @run main serialver_zh_CNTest
 */
public class serialver_zh_CNTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tools.serialver.resources.serialver_zh_CN", false, serialver_zh_CNTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.tools.serialver.resources.serialver_zh_CN"), "name");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
