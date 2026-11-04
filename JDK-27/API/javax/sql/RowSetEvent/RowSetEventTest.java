/*
 * @test
 * @summary javax.sql.RowSetEvent conformance: white-box structure + black-box behavior.
 * @run main RowSetEventTest
 */
public class RowSetEventTest {
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
        Class<?> c = Class.forName("javax.sql.RowSetEvent");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 0, "declared method count 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.sql.RowSet rs = (javax.sql.RowSet) java.lang.reflect.Proxy.newProxyInstance(
            RowSetEventTest.class.getClassLoader(), new Class[]{javax.sql.RowSet.class},
            (p, m, a) -> { if (m.getReturnType() == boolean.class) return false; if (m.getReturnType() == int.class) return 0; return null; });
        javax.sql.RowSetEvent ev = new javax.sql.RowSetEvent(rs);
        check(ev.getSource() == rs, "getSource");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
