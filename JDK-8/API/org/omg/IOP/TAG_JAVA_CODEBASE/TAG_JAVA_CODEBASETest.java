/*
 * @test
 * @summary org.omg.IOP.TAG_JAVA_CODEBASE conformance: constant holder interface.
 * @run main TAG_JAVA_CODEBASETest
 */

public class TAG_JAVA_CODEBASETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_JAVA_CODEBASE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ComponentId constant exact contract ----
        check(org.omg.IOP.TAG_JAVA_CODEBASE.value == 25, "TAG_JAVA_CODEBASE.value == 25 (Java-specific range)");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.TAG_JAVA_CODEBASE.value >= 20, "SUN-reserved component id range starts at 20");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
