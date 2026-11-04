/*
 * @test
 * @summary java.sql.PreparedStatement conformance: white-box structure + black-box behavior.
 * @run main PreparedStatementTest
 */
public class PreparedStatementTest {
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
        Class<?> c = Class.forName("java.sql.PreparedStatement");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.Statement"), "superinterface 0");
        check(c.getDeclaredMethods().length == 58, "declared method count 58");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
java.sql.PreparedStatement ps = (java.sql.PreparedStatement) java.lang.reflect.Proxy.newProxyInstance(PreparedStatementTest.class.getClassLoader(), new Class[]{java.sql.PreparedStatement.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
checkThrows(java.sql.SQLFeatureNotSupportedException.class, "setObject SQLType default", () -> ps.setObject(1, "x", java.sql.JDBCType.VARCHAR));
checkThrows(UnsupportedOperationException.class, "executeLargeUpdate default", () -> ps.executeLargeUpdate());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
