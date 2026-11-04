/*
 * @test
 * @summary javax.management.MBeanNotificationInfo conformance: notification types.
 * @run main MBeanNotificationInfoTest
 */

import javax.management.*;
public class MBeanNotificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanNotificationInfo n = new MBeanNotificationInfo(new String[]{"a.b", "c.d"}, "note", "desc");
        check(n.getNotifTypes().length == 2 && "a.b".equals(n.getNotifTypes()[0]), "notif types");
        check("note".equals(n.getName()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
