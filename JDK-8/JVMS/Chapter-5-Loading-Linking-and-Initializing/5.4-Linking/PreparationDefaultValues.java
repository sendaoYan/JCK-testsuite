/*
 * @test
 * @summary JVMS 5.4.2: during preparation static fields are set to their
 *          default values before any initializer runs, so an initializer that
 *          reads a not-yet-assigned field (through a method, avoiding an
 *          illegal forward reference) observes the default 0 or null.
 * @run main PreparationDefaultValues
 */

public class PreparationDefaultValues {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int x = readY();   // reads y before y's initializer -> default 0
    static int y = 10;
    static int readY() { return y; }

    static String s = readS2();   // reads s2 before its initializer -> default null
    static String s2 = "done";
    static String readS2() { return s2; }

    public static void main(String[] args) {
        check(x == 0, "int field observes its prepared default 0");
        check(y == 10, "later int initializer still runs");
        check(s == null, "reference field observes its prepared default null");
        check("done".equals(s2), "later reference initializer still runs");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
