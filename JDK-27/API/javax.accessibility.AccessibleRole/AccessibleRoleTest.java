/*
 * @test
 * @summary javax.accessibility.AccessibleRole conformance: accessible role constants.
 * @run main AccessibleRoleTest
 */

public class AccessibleRoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.accessibility.AccessibleRole alert = javax.accessibility.AccessibleRole.ALERT;
        check(alert != null, "ALERT");
        check(javax.accessibility.AccessibleRole.PUSH_BUTTON != null, "PUSH_BUTTON");
        check(javax.accessibility.AccessibleRole.TABLE != null, "TABLE");
        check(javax.accessibility.AccessibleRole.UNKNOWN != null, "UNKNOWN");
        check(alert != javax.accessibility.AccessibleRole.PUSH_BUTTON, "distinct constants");
        check(alert.toDisplayString() != null, "toDisplayString");
        check(alert.toString().length() > 0, "toString non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
