/*
 * @test
 * @summary javax.sql.RowSetListener conformance: white-box structure + black-box behavior.
 * @run main RowSetListenerTest
 */
public class RowSetListenerTest {
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
        Class<?> c = Class.forName("javax.sql.RowSetListener");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.util.EventListener"), "superinterface 0");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] n = {0};
        javax.sql.RowSetListener l = new javax.sql.RowSetListener() {
            public void rowSetChanged(javax.sql.RowSetEvent e) { n[0]++; }
            public void rowChanged(javax.sql.RowSetEvent e) { n[0]++; }
            public void cursorMoved(javax.sql.RowSetEvent e) { n[0]++; }
        };
        javax.sql.RowSet rs = (javax.sql.RowSet) java.lang.reflect.Proxy.newProxyInstance(
            RowSetListenerTest.class.getClassLoader(), new Class[]{javax.sql.RowSet.class},
            (p, m, a) -> { if (m.getReturnType() == boolean.class) return false; if (m.getReturnType() == int.class) return 0; return null; });
        javax.sql.RowSetEvent ev = new javax.sql.RowSetEvent(rs);
        l.rowSetChanged(ev); l.rowChanged(ev); l.cursorMoved(ev);
        check(n[0] == 3, "listener methods called");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
