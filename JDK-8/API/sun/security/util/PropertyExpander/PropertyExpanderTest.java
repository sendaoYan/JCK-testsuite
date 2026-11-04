/*
 * @test
 * @summary sun.security.util.PropertyExpander conformance: type shape + behavioral (headless-safe).
 * @run main PropertyExpanderTest
 */

public class PropertyExpanderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.PropertyExpander", false, PropertyExpanderTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        String ex = sun.security.util.PropertyExpander.expand("${java.home}");
        check(ex != null && !ex.contains("${"), "PropertyExpander expand");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
