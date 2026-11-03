/*
 * @test
 * @summary java.sql.Clob conformance: white-box structure + black-box behavior.
 * @run main ClobTest
 */
public class ClobTest {
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
        Class<?> c = Class.forName("java.sql.Clob");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.lang.AutoCloseable"), "superinterface 0");
        check(c.getDeclaredMethods().length == 14, "declared method count 14");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.sql.rowset.serial.SerialClob c = new javax.sql.rowset.serial.SerialClob(new char[]{'a', 'b', 'c', 'd'});
        check(c.length() == 4, "length");
        check("abcd".equals(c.getSubString(1, 4)), "getSubString full");
        check("bc".equals(c.getSubString(2, 2)), "getSubString slice");
        check(c.position("bc", 1) == 2, "position found");
        check(c.position("zz", 1) == -1, "position absent");
        c.truncate(2);
        check(c.length() == 2, "truncate");
        c.free();
        check(java.sql.Clob.class.isAssignableFrom(javax.sql.rowset.serial.SerialClob.class), "SerialClob implements Clob");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
