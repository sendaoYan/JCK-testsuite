/*
 * @test
 * @summary java.sql.PseudoColumnUsage conformance: white-box structure + black-box behavior.
 * @run main PseudoColumnUsageTest
 */
public class PseudoColumnUsageTest {
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
        Class<?> c = Class.forName("java.sql.PseudoColumnUsage");
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
        java.sql.PseudoColumnUsage[] all = java.sql.PseudoColumnUsage.values();
        check(all.length == 4, "values() length 4");
        check(java.sql.PseudoColumnUsage.SELECT_LIST_ONLY == all[0], "ordinal SELECT_LIST_ONLY");
        check("SELECT_LIST_ONLY".equals(java.sql.PseudoColumnUsage.SELECT_LIST_ONLY.name()), "name SELECT_LIST_ONLY");
        check(java.sql.PseudoColumnUsage.valueOf("SELECT_LIST_ONLY") == java.sql.PseudoColumnUsage.SELECT_LIST_ONLY, "valueOf SELECT_LIST_ONLY");
        check(java.sql.PseudoColumnUsage.WHERE_CLAUSE_ONLY == all[1], "ordinal WHERE_CLAUSE_ONLY");
        check("WHERE_CLAUSE_ONLY".equals(java.sql.PseudoColumnUsage.WHERE_CLAUSE_ONLY.name()), "name WHERE_CLAUSE_ONLY");
        check(java.sql.PseudoColumnUsage.valueOf("WHERE_CLAUSE_ONLY") == java.sql.PseudoColumnUsage.WHERE_CLAUSE_ONLY, "valueOf WHERE_CLAUSE_ONLY");
        check(java.sql.PseudoColumnUsage.NO_USAGE_RESTRICTIONS == all[2], "ordinal NO_USAGE_RESTRICTIONS");
        check("NO_USAGE_RESTRICTIONS".equals(java.sql.PseudoColumnUsage.NO_USAGE_RESTRICTIONS.name()), "name NO_USAGE_RESTRICTIONS");
        check(java.sql.PseudoColumnUsage.valueOf("NO_USAGE_RESTRICTIONS") == java.sql.PseudoColumnUsage.NO_USAGE_RESTRICTIONS, "valueOf NO_USAGE_RESTRICTIONS");
        check(java.sql.PseudoColumnUsage.USAGE_UNKNOWN == all[3], "ordinal USAGE_UNKNOWN");
        check("USAGE_UNKNOWN".equals(java.sql.PseudoColumnUsage.USAGE_UNKNOWN.name()), "name USAGE_UNKNOWN");
        check(java.sql.PseudoColumnUsage.valueOf("USAGE_UNKNOWN") == java.sql.PseudoColumnUsage.USAGE_UNKNOWN, "valueOf USAGE_UNKNOWN");
        checkThrows(IllegalArgumentException.class, "valueOf invalid", () -> java.sql.PseudoColumnUsage.valueOf("NOPE"));
        check(java.util.Arrays.equals(all, java.sql.PseudoColumnUsage.values()), "values() deterministic order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
