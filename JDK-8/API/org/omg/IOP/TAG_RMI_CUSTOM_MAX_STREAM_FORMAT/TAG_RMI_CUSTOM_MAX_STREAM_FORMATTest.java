/*
 * @test
 * @summary org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT conformance: constant holder interface.
 * @run main TAG_RMI_CUSTOM_MAX_STREAM_FORMATTest
 */

public class TAG_RMI_CUSTOM_MAX_STREAM_FORMATTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ComponentId constant exact contract ----
        check(org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value == 38, "TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value == 38");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value != org.omg.IOP.RMICustomMaxStreamFormat.value,
              "component TAG_RMI_CUSTOM_MAX_STREAM_FORMAT(38) != service-context RMICustomMaxStreamFormat(17)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
