/*
 * @test
 * @summary sun.security.util.DerValue conformance: type shape + behavioral (headless-safe).
 * @run main DerValueTest
 */

public class DerValueTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.DerValue", false, DerValueTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(sun.security.util.DerValue.tag_Boolean == 1, "DerValue tag_Boolean");
        check(sun.security.util.DerValue.tag_Integer == 2, "DerValue tag_Integer");
        check(sun.security.util.DerValue.tag_ObjectId == 6, "DerValue tag_ObjectId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
