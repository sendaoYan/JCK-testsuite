/*
 * @test
 * @summary java.sql.ClientInfoStatus conformance: white-box structure + black-box behavior.
 * @run main ClientInfoStatusTest
 */
public class ClientInfoStatusTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.ClientInfoStatus");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.ClientInfoStatus[] all = java.sql.ClientInfoStatus.values();
        check(all.length == 4, "values() length 4");
        check(java.sql.ClientInfoStatus.REASON_UNKNOWN == all[0], "ordinal REASON_UNKNOWN");
        check("REASON_UNKNOWN".equals(java.sql.ClientInfoStatus.REASON_UNKNOWN.name()), "name REASON_UNKNOWN");
        check(java.sql.ClientInfoStatus.valueOf("REASON_UNKNOWN") == java.sql.ClientInfoStatus.REASON_UNKNOWN, "valueOf REASON_UNKNOWN");
        check(java.sql.ClientInfoStatus.REASON_UNKNOWN_PROPERTY == all[1], "ordinal REASON_UNKNOWN_PROPERTY");
        check("REASON_UNKNOWN_PROPERTY".equals(java.sql.ClientInfoStatus.REASON_UNKNOWN_PROPERTY.name()), "name REASON_UNKNOWN_PROPERTY");
        check(java.sql.ClientInfoStatus.valueOf("REASON_UNKNOWN_PROPERTY") == java.sql.ClientInfoStatus.REASON_UNKNOWN_PROPERTY, "valueOf REASON_UNKNOWN_PROPERTY");
        check(java.sql.ClientInfoStatus.REASON_VALUE_INVALID == all[2], "ordinal REASON_VALUE_INVALID");
        check("REASON_VALUE_INVALID".equals(java.sql.ClientInfoStatus.REASON_VALUE_INVALID.name()), "name REASON_VALUE_INVALID");
        check(java.sql.ClientInfoStatus.valueOf("REASON_VALUE_INVALID") == java.sql.ClientInfoStatus.REASON_VALUE_INVALID, "valueOf REASON_VALUE_INVALID");
        check(java.sql.ClientInfoStatus.REASON_VALUE_TRUNCATED == all[3], "ordinal REASON_VALUE_TRUNCATED");
        check("REASON_VALUE_TRUNCATED".equals(java.sql.ClientInfoStatus.REASON_VALUE_TRUNCATED.name()), "name REASON_VALUE_TRUNCATED");
        check(java.sql.ClientInfoStatus.valueOf("REASON_VALUE_TRUNCATED") == java.sql.ClientInfoStatus.REASON_VALUE_TRUNCATED, "valueOf REASON_VALUE_TRUNCATED");
        checkThrows(IllegalArgumentException.class, "valueOf invalid", () -> java.sql.ClientInfoStatus.valueOf("NOPE"));
        check(java.util.Arrays.equals(all, java.sql.ClientInfoStatus.values()), "values() deterministic order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
