/*
 * @test
 * @summary org.omg.IOP.TAG_MULTIPLE_COMPONENTS conformance: constant holder interface.
 * @run main TAG_MULTIPLE_COMPONENTSTest
 */

public class TAG_MULTIPLE_COMPONENTSTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_MULTIPLE_COMPONENTS");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ProfileId constant exact contract ----
        check(org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value == 1, "TAG_MULTIPLE_COMPONENTS.value == 1");
        check(c.getDeclaredFields().length == 1, "exactly one declared field (single-constant interface)");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value - org.omg.IOP.TAG_INTERNET_IOP.value == 1,
              "TAG_MULTIPLE_COMPONENTS is TAG_INTERNET_IOP + 1 (contiguous reserved range)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
