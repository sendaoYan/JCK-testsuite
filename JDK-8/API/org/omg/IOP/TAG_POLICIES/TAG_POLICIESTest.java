/*
 * @test
 * @summary org.omg.IOP.TAG_POLICIES conformance: constant holder interface.
 * @run main TAG_POLICIESTest
 */

public class TAG_POLICIESTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_POLICIES");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ComponentId constant exact contract ----
        check(org.omg.IOP.TAG_POLICIES.value == 2, "TAG_POLICIES.value == 2 (ComponentId family)");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.TAG_POLICIES.value == 2 && org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value == 3,
              "TAG_POLICIES=2 directly precedes TAG_ALTERNATE_IIOP_ADDRESS=3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
