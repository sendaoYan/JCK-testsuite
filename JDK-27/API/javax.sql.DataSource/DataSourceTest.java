/*
 * @test
 * @summary javax.sql.DataSource conformance: white-box structure + black-box behavior.
 * @run main DataSourceTest
 */
public class DataSourceTest {
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
        Class<?> c = Class.forName("javax.sql.DataSource");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.CommonDataSource"), "superinterface 0");
        check(ifs[1].getName().equals("java.sql.Wrapper"), "superinterface 1");
        check(c.getDeclaredMethods().length == 7, "declared method count 7");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
javax.sql.DataSource ds = (javax.sql.DataSource) java.lang.reflect.Proxy.newProxyInstance(DataSourceTest.class.getClassLoader(), new Class[]{javax.sql.DataSource.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
checkThrows(java.sql.SQLFeatureNotSupportedException.class, "createConnectionBuilder default", () -> ds.createConnectionBuilder());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
