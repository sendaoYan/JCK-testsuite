/*
 * @test
 * @summary java.sql.Connection conformance: white-box structure + black-box behavior.
 * @run main ConnectionTest
 */
public class ConnectionTest {
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
        Class<?> c = Class.forName("java.sql.Connection");
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
        check(c.getDeclaredMethods().length == 62, "declared method count 62");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
java.sql.Connection c = (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(ConnectionTest.class.getClassLoader(), new Class[]{java.sql.Connection.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
check("'a''b'".equals(c.enquoteLiteral("a'b")), "enquoteLiteral");
check("N'x''y'".equals(c.enquoteNCharLiteral("x'y")), "enquoteNCharLiteral");
check(c.isSimpleIdentifier("abc"), "isSimpleIdentifier true");
check(!c.isSimpleIdentifier("_abc"), "isSimpleIdentifier underscore false");
c.beginRequest(); c.endRequest(); check(true, "beginRequest/endRequest no-op");
check(java.sql.Connection.TRANSACTION_NONE == 0, "TRANSACTION_NONE");
check(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED == 1, "TRANSACTION_READ_UNCOMMITTED");
check(java.sql.Connection.TRANSACTION_READ_COMMITTED == 2, "TRANSACTION_READ_COMMITTED");
check(java.sql.Connection.TRANSACTION_REPEATABLE_READ == 4, "TRANSACTION_REPEATABLE_READ");
check(java.sql.Connection.TRANSACTION_SERIALIZABLE == 8, "TRANSACTION_SERIALIZABLE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
