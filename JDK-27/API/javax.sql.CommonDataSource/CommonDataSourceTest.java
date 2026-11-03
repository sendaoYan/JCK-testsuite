/*
 * @test
 * @summary javax.sql.CommonDataSource conformance: white-box structure + black-box behavior.
 * @run main CommonDataSourceTest
 */
public class CommonDataSourceTest {
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
        Class<?> c = Class.forName("javax.sql.CommonDataSource");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 6, "declared method count 6");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
javax.sql.CommonDataSource cd = (javax.sql.CommonDataSource) java.lang.reflect.Proxy.newProxyInstance(CommonDataSourceTest.class.getClassLoader(), new Class[]{javax.sql.CommonDataSource.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
checkThrows(java.sql.SQLFeatureNotSupportedException.class, "createShardingKeyBuilder default", () -> cd.createShardingKeyBuilder());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
