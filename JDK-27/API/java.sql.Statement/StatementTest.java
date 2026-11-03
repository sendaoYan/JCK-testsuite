/*
 * @test
 * @summary java.sql.Statement conformance: white-box structure + black-box behavior.
 * @run main StatementTest
 */
public class StatementTest {
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
        Class<?> c = Class.forName("java.sql.Statement");
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
        check(c.getDeclaredMethods().length == 54, "declared method count 54");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
java.sql.Statement s = (java.sql.Statement) java.lang.reflect.Proxy.newProxyInstance(StatementTest.class.getClassLoader(), new Class[]{java.sql.Statement.class}, (p, m, a) -> java.lang.reflect.InvocationHandler.invokeDefault(p, m, a));
check("'a''b'".equals(s.enquoteLiteral("a'b")), "enquoteLiteral");
check("N'a''b'".equals(s.enquoteNCharLiteral("a'b")), "enquoteNCharLiteral");
check(s.isSimpleIdentifier("abc"), "isSimpleIdentifier");
check(java.sql.Statement.CLOSE_CURRENT_RESULT == 1, "CLOSE_CURRENT_RESULT");
check(java.sql.Statement.KEEP_CURRENT_RESULT == 2, "KEEP_CURRENT_RESULT");
check(java.sql.Statement.CLOSE_ALL_RESULTS == 3, "CLOSE_ALL_RESULTS");
check(java.sql.Statement.SUCCESS_NO_INFO == -2, "SUCCESS_NO_INFO");
check(java.sql.Statement.EXECUTE_FAILED == -3, "EXECUTE_FAILED");
check(java.sql.Statement.RETURN_GENERATED_KEYS == 1, "RETURN_GENERATED_KEYS");
check(java.sql.Statement.NO_GENERATED_KEYS == 2, "NO_GENERATED_KEYS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
