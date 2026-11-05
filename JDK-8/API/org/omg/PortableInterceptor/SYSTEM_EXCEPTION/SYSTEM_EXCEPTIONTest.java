/*
 * @test
 * @summary org.omg.PortableInterceptor.SYSTEM_EXCEPTION conformance: constant holder interface.
 * @run main SYSTEM_EXCEPTIONTest
 */

public class SYSTEM_EXCEPTIONTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.SYSTEM_EXCEPTION");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        // ---- deepened (jdk8u372-b07 probed): reply_status constant exact contract ----
        check(org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value == (short) 1, "SYSTEM_EXCEPTION.value == 1");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == short.class, "value is short");
        // full reply_status family as materialized in this JDK: SUCCESSFUL=0 SYSTEM_EXCEPTION=1
        // USER_EXCEPTION=2 LOCATION_FORWARD=3 TRANSPORT_RETRY=4 (five distinct, contiguous values)
        check(org.omg.PortableInterceptor.SUCCESSFUL.value == 0
              && org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value == 1
              && org.omg.PortableInterceptor.USER_EXCEPTION.value == 2
              && org.omg.PortableInterceptor.LOCATION_FORWARD.value == 3
              && org.omg.PortableInterceptor.TRANSPORT_RETRY.value == 4,
              "reply_status family exact: 0,1,2,3,4");
        check(org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value == 1
              && org.omg.PortableInterceptor.USER_EXCEPTION.value == 2,
              "JDK-8 materializes SYSTEM_EXCEPTION=1 / USER_EXCEPTION=2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
