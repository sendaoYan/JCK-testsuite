/*
 * @test
 * @summary org.omg.PortableServer.THREAD_POLICY_ID conformance: constant holder interface.
 * @run main THREAD_POLICY_IDTest
 */

public class THREAD_POLICY_IDTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.THREAD_POLICY_ID");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): exact policy type id + constant-only interface shape ----
        check(org.omg.PortableServer.THREAD_POLICY_ID.value == 16, "THREAD_POLICY_ID.value == 16 (POLICY_TYPE_ID table)");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(c.getDeclaredFields().length == 1, "constant-only interface: exactly one declared field");
        check(c.getDeclaredMethods().length == 0, "constant-only interface: no methods declared");
        check(f.get(null).equals(Integer.valueOf(16)), "reflective read back of value == 16");
        check(org.omg.PortableServer.THREAD_POLICY_ID.value == 16
           && org.omg.PortableServer.LIFESPAN_POLICY_ID.value == 17
           && org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID.value == 18
           && org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID.value == 19
           && org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID.value == 20
           && org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID.value == 21
           && org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value == 22,
              "PortableServer POLICY_TYPE_ID contiguous table 16..22 (all distinct)");
        // ORB 策略工厂负例（离线 ORB，jdk8u372-b07 实测）：无 PolicyFactory 注册
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        try {
            orb.create_policy(org.omg.PortableServer.THREAD_POLICY_ID.value, orb.create_any());
            check(false, "create_policy(16) on offline ORB must throw PolicyError");
        } catch (org.omg.CORBA.PolicyError expected) {
            check(org.omg.CORBA.UserException.class.isInstance(expected), "PolicyError is a UserException");
            check(expected.getMessage().equals("There is no PolicyFactory Registered for type 16"),
                  "PolicyError message exact for type 16");
        }
        try {
            orb.create_policy(-1, orb.create_any());
            check(false, "create_policy(-1) must throw PolicyError");
        } catch (org.omg.CORBA.PolicyError expected) {
            check(expected.getMessage().equals("There is no PolicyFactory Registered for type -1"),
                  "PolicyError message exact for type -1");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
