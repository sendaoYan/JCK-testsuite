/*
 * @test
 * @summary sun.tools.serialver.resources.serialver_ja conformance: internal class reflection.
 * @modules jdk.compiler/sun.tools.serialver.resources:+open
 * @run main serialver_jaTest
 */
public class serialver_jaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tools.serialver.resources.serialver_ja", false, serialver_jaTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.tools.serialver.resources.serialver_ja"), "name");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
