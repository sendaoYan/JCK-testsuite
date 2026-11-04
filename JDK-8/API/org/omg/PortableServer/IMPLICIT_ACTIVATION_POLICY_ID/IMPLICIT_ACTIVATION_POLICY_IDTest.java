/*
 * @test
 * @summary org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID conformance: constant holder interface.
 * @run main IMPLICIT_ACTIVATION_POLICY_IDTest
 */

public class IMPLICIT_ACTIVATION_POLICY_IDTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
