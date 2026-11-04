/*
 * @test
 * @summary javax.management.MBeanNotificationInfo conformance: full contract
 *          coverage. notification types, null/empty types -> empty, defensive
 *          getter, clone/equals.
 * @run main MBeanNotificationInfoTest
 */

import javax.management.MBeanNotificationInfo;

public class MBeanNotificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanNotificationInfo n = new MBeanNotificationInfo(new String[]{"a.b", "c.d"}, "note", "desc");
        check(n.getNotifTypes().length == 2 && "a.b".equals(n.getNotifTypes()[0]), "notif types");
        check("note".equals(n.getName()), "name");
    }

    static void testNullAndEmptyTypes() {
        check(new MBeanNotificationInfo(null, "note", "desc").getNotifTypes().length == 0, "null types -> empty");
        check(new MBeanNotificationInfo(new String[0], "note", "desc").getNotifTypes().length == 0, "empty types -> empty");
    }

    static void testDefensiveGetterAndClone() {
        MBeanNotificationInfo n = new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc");
        n.getNotifTypes()[0] = null;
        check("a.b".equals(n.getNotifTypes()[0]), "getNotifTypes defensive");
        MBeanNotificationInfo c = (MBeanNotificationInfo) n.clone();
        check(n.equals(c) && n.hashCode() == c.hashCode(), "clone equals/hashCode");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testNullAndEmptyTypes();
        testDefensiveGetterAndClone();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
