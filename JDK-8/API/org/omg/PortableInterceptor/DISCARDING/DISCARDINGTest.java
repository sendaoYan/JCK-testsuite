/*
 * @test
 * @summary org.omg.PortableInterceptor.DISCARDING conformance: constant holder interface.
 * @run main DISCARDINGTest
 */

public class DISCARDINGTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.DISCARDING");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        // ---- deepened (jdk8u372-b07 probed): adapter state constant exact contract ----
        check(org.omg.PortableInterceptor.DISCARDING.value == (short) 2, "DISCARDING.value == 2");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == short.class, "value is short");
        // full adapter-state family: HOLDING=0 ACTIVE=1 DISCARDING=2 INACTIVE=3 NON_EXISTENT=4
        check(org.omg.PortableInterceptor.HOLDING.value == 0
              && org.omg.PortableInterceptor.ACTIVE.value == 1
              && org.omg.PortableInterceptor.DISCARDING.value == 2
              && org.omg.PortableInterceptor.INACTIVE.value == 3
              && org.omg.PortableInterceptor.NON_EXISTENT.value == 4,
              "adapter state family exact: 0,1,2,3,4");
        check(org.omg.PortableInterceptor.AdapterStateHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/AdapterState:1.0"),
              "AdapterState typedef repository id exact");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
