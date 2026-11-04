/*
 * @test
 * @summary javax.sql.ConnectionPoolDataSource conformance: white-box structure + black-box behavior.
 * @run main ConnectionPoolDataSourceTest
 */
public class ConnectionPoolDataSourceTest {
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
        Class<?> c = Class.forName("javax.sql.ConnectionPoolDataSource");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.CommonDataSource"), "superinterface 0");
        check(c.getDeclaredMethods().length == 7, "declared method count 7");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
javax.sql.ConnectionPoolDataSource cp = (javax.sql.ConnectionPoolDataSource) java.lang.reflect.Proxy.newProxyInstance(ConnectionPoolDataSourceTest.class.getClassLoader(), new Class[]{javax.sql.ConnectionPoolDataSource.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
checkThrows(java.sql.SQLFeatureNotSupportedException.class, "createPooledConnectionBuilder default", () -> cp.createPooledConnectionBuilder());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
