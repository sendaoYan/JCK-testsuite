/*
 * @test
 * @summary jdk.internal.joptsimple.AbstractOptionSpec conformance: internal class reflection (non-root module).
 * @modules jdk.internal.opt/jdk.internal.joptsimple:+open
 * @run main AbstractOptionSpecTest
 */
public class AbstractOptionSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.joptsimple.AbstractOptionSpec", false, AbstractOptionSpecTest.class.getClassLoader());
        check(c != null, "loads");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
