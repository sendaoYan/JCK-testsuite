/*
 * @test
 * @summary JLS 15.16 (with 5.5) (Java SE 8): a reference cast performs a
 *          run-time type check, throwing ClassCastException on failure; casting
 *          null never throws; numeric casts truncate toward zero.
 * @run main CastRuntimeCheck
 */

public class CastRuntimeCheck {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Object o = "hello";
        String s = (String) o;
        check(s.equals("hello"), "valid downcast");

        Object n = Integer.valueOf(1);
        try {
            String bad = (String) n;
            throw new AssertionError("ClassCastException expected");
        } catch (ClassCastException expected) {
            passed++;
        }

        Object nil = null;
        check((String) nil == null, "casting null yields null");

        check((int) 3.99 == 3, "narrowing double->int truncates toward zero");
        check((int) -3.99 == -3, "narrowing negative truncates toward zero");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
