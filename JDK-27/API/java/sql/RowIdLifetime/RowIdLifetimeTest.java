/*
 * @test
 * @summary java.sql.RowIdLifetime conformance: white-box structure + black-box behavior.
 * @run main RowIdLifetimeTest
 */
public class RowIdLifetimeTest {
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
        Class<?> c = Class.forName("java.sql.RowIdLifetime");
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
        java.sql.RowIdLifetime[] all = java.sql.RowIdLifetime.values();
        check(all.length == 5, "values() length 5");
        check(java.sql.RowIdLifetime.ROWID_UNSUPPORTED == all[0], "ordinal ROWID_UNSUPPORTED");
        check("ROWID_UNSUPPORTED".equals(java.sql.RowIdLifetime.ROWID_UNSUPPORTED.name()), "name ROWID_UNSUPPORTED");
        check(java.sql.RowIdLifetime.valueOf("ROWID_UNSUPPORTED") == java.sql.RowIdLifetime.ROWID_UNSUPPORTED, "valueOf ROWID_UNSUPPORTED");
        check(java.sql.RowIdLifetime.ROWID_VALID_OTHER == all[1], "ordinal ROWID_VALID_OTHER");
        check("ROWID_VALID_OTHER".equals(java.sql.RowIdLifetime.ROWID_VALID_OTHER.name()), "name ROWID_VALID_OTHER");
        check(java.sql.RowIdLifetime.valueOf("ROWID_VALID_OTHER") == java.sql.RowIdLifetime.ROWID_VALID_OTHER, "valueOf ROWID_VALID_OTHER");
        check(java.sql.RowIdLifetime.ROWID_VALID_SESSION == all[2], "ordinal ROWID_VALID_SESSION");
        check("ROWID_VALID_SESSION".equals(java.sql.RowIdLifetime.ROWID_VALID_SESSION.name()), "name ROWID_VALID_SESSION");
        check(java.sql.RowIdLifetime.valueOf("ROWID_VALID_SESSION") == java.sql.RowIdLifetime.ROWID_VALID_SESSION, "valueOf ROWID_VALID_SESSION");
        check(java.sql.RowIdLifetime.ROWID_VALID_TRANSACTION == all[3], "ordinal ROWID_VALID_TRANSACTION");
        check("ROWID_VALID_TRANSACTION".equals(java.sql.RowIdLifetime.ROWID_VALID_TRANSACTION.name()), "name ROWID_VALID_TRANSACTION");
        check(java.sql.RowIdLifetime.valueOf("ROWID_VALID_TRANSACTION") == java.sql.RowIdLifetime.ROWID_VALID_TRANSACTION, "valueOf ROWID_VALID_TRANSACTION");
        check(java.sql.RowIdLifetime.ROWID_VALID_FOREVER == all[4], "ordinal ROWID_VALID_FOREVER");
        check("ROWID_VALID_FOREVER".equals(java.sql.RowIdLifetime.ROWID_VALID_FOREVER.name()), "name ROWID_VALID_FOREVER");
        check(java.sql.RowIdLifetime.valueOf("ROWID_VALID_FOREVER") == java.sql.RowIdLifetime.ROWID_VALID_FOREVER, "valueOf ROWID_VALID_FOREVER");
        checkThrows(IllegalArgumentException.class, "valueOf invalid", () -> java.sql.RowIdLifetime.valueOf("NOPE"));
        check(java.util.Arrays.equals(all, java.sql.RowIdLifetime.values()), "values() deterministic order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
