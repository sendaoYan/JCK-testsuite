/*
 * @test
 * @summary JVMS 2.6.1: a long and a double each occupy two local variable slots,
 *          so the JVM passes a (long, double, int) argument list with the long
 *          in slots 0-1, the double in slots 2-3 and the int in slot 4.
 * @compile LongDoubleTwoSlots.jasm
 * @run main LongDoubleTwoSlotsTest
 */

public class LongDoubleTwoSlotsTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("LongDoubleTwoSlots");
        java.lang.reflect.Method m = c.getMethod("sum",
                long.class, double.class, int.class);

        // 10L + (long)2.9 + 5 == 10 + 2 + 5 == 17
        Object r = m.invoke(null, 10L, 2.9, 5);
        check(r.equals(17L), "long+double+int sum with two-slot types");

        // Negative double truncates toward zero: 10L + (long)(-2.9) + 5 == 13
        check(m.invoke(null, 10L, -2.9, 5).equals(13L),
                "negative double truncates toward zero");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
