/*
 * @test
 * @summary JLS 15.11 (Java SE 8): a static field may be accessed through an
 *          instance reference; the primary is evaluated for side effects but its
 *          value is discarded, so even a null reference does not throw.
 * @run main StaticFieldViaInstanceReference
 */

public class StaticFieldViaInstanceReference {
    static int passed;
    static int counter = 0;
    static int stat = 42;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StaticFieldViaInstanceReference make() { counter++; return null; }

    public static void main(String[] args) {
        StaticFieldViaInstanceReference ref = new StaticFieldViaInstanceReference();
        check(ref.stat == 42, "instance reference to static field reads the static value");
        check(make().stat == 42, "static field via null reference does not throw");
        check(counter == 1, "primary evaluated exactly once");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
