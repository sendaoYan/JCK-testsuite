/*
 * @test
 * @summary org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID conformance: constant holder interface.
 * @run main REQUEST_PROCESSING_POLICY_IDTest
 */

public class REQUEST_PROCESSING_POLICY_IDTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): exact policy type id + constant-only interface shape ----
        check(org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value == 22, "REQUEST_PROCESSING_POLICY_ID.value == 22 (POLICY_TYPE_ID table)");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(c.getDeclaredFields().length == 1, "constant-only interface: exactly one declared field");
        check(c.getDeclaredMethods().length == 0, "constant-only interface: no methods declared");
        check(f.get(null).equals(Integer.valueOf(22)), "reflective read back of value == 22");
        check(org.omg.PortableServer.THREAD_POLICY_ID.value == 16
           && org.omg.PortableServer.LIFESPAN_POLICY_ID.value == 17
           && org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID.value == 18
           && org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID.value == 19
           && org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID.value == 20
           && org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID.value == 21
           && org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value == 22,
              "PortableServer POLICY_TYPE_ID contiguous table 16..22 (all distinct)");


        System.out.println("PASS (" + passed + " assertions)");
    }
}
