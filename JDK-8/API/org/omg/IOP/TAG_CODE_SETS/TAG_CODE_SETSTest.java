/*
 * @test
 * @summary org.omg.IOP.TAG_CODE_SETS conformance: constant holder interface.
 * @run main TAG_CODE_SETSTest
 */

public class TAG_CODE_SETSTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_CODE_SETS");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ComponentId constant exact contract ----
        check(org.omg.IOP.TAG_CODE_SETS.value == 1, "TAG_CODE_SETS.value == 1 (ComponentId family)");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.TAG_CODE_SETS.value == org.omg.IOP.CodeSets.value,
              "component TAG_CODE_SETS(1) == service-context CodeSets(1): same OMG number in both namespaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
