/*
 * @test
 * @summary org.omg.CORBA.BAD_POLICY_TYPE conformance: constant holder interface.
 * @run main BAD_POLICY_TYPETest
 */

public class BAD_POLICY_TYPETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.BAD_POLICY_TYPE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
