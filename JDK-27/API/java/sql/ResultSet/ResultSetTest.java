/*
 * @test
 * @summary java.sql.ResultSet conformance: white-box structure + black-box behavior.
 * @run main ResultSetTest
 */
public class ResultSetTest {
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
        Class<?> c = Class.forName("java.sql.ResultSet");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Wrapper"), "superinterface 0");
        check(ifs[1].getName().equals("java.lang.AutoCloseable"), "superinterface 1");
        check(c.getDeclaredMethods().length == 193, "declared method count 193");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
check(java.sql.ResultSet.FETCH_FORWARD == 1000, "FETCH_FORWARD");
check(java.sql.ResultSet.FETCH_REVERSE == 1001, "FETCH_REVERSE");
check(java.sql.ResultSet.FETCH_UNKNOWN == 1002, "FETCH_UNKNOWN");
check(java.sql.ResultSet.TYPE_FORWARD_ONLY == 1003, "TYPE_FORWARD_ONLY");
check(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE == 1004, "TYPE_SCROLL_INSENSITIVE");
check(java.sql.ResultSet.TYPE_SCROLL_SENSITIVE == 1005, "TYPE_SCROLL_SENSITIVE");
check(java.sql.ResultSet.CONCUR_READ_ONLY == 1007, "CONCUR_READ_ONLY");
check(java.sql.ResultSet.CONCUR_UPDATABLE == 1008, "CONCUR_UPDATABLE");
check(java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT == 1, "HOLD_CURSORS_OVER_COMMIT");
check(java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT == 2, "CLOSE_CURSORS_AT_COMMIT");
java.sql.ResultSet rs = (java.sql.ResultSet) java.lang.reflect.Proxy.newProxyInstance(ResultSetTest.class.getClassLoader(), new Class[]{java.sql.ResultSet.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
checkThrows(java.sql.SQLFeatureNotSupportedException.class, "updateObject default", () -> rs.updateObject(1, "x", java.sql.JDBCType.VARCHAR));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
