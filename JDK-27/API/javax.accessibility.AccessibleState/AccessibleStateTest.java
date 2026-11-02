/*
 * @test
 * @summary javax.accessibility.AccessibleState conformance: accessible state constants.
 * @run main AccessibleStateTest
 */

public class AccessibleStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.accessibility.AccessibleState active = javax.accessibility.AccessibleState.ACTIVE;
        check(active != null, "ACTIVE");
        check(javax.accessibility.AccessibleState.ENABLED != null, "ENABLED");
        check(javax.accessibility.AccessibleState.VISIBLE != null, "VISIBLE");
        check(javax.accessibility.AccessibleState.SHOWING != null, "SHOWING");
        check(active != javax.accessibility.AccessibleState.ENABLED, "distinct constants");
        check(active.toDisplayString() != null, "toDisplayString");
        check(active.toString().length() > 0, "toString non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
