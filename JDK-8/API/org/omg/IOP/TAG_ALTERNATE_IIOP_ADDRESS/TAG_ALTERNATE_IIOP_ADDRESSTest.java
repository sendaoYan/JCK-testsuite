/*
 * @test
 * @summary org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS conformance: constant holder interface.
 * @run main TAG_ALTERNATE_IIOP_ADDRESSTest
 */

public class TAG_ALTERNATE_IIOP_ADDRESSTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ComponentId constant exact contract ----
        check(org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value == 3, "TAG_ALTERNATE_IIOP_ADDRESS.value == 3");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
