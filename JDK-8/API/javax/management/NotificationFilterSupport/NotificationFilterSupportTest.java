/*
 * @test
 * @summary javax.management.NotificationFilterSupport conformance: enableType filtering.
 * @run main NotificationFilterSupportTest
 */

import javax.management.*;
public class NotificationFilterSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a.b");
        check(f.isNotificationEnabled(new Notification("a.b", "src", 1L)), "enabled type");
        check(!f.isNotificationEnabled(new Notification("c.d", "src", 1L)), "disabled type");
        check(f.getEnabledTypes().contains("a.b"), "getEnabledTypes");
        f.disableType("a.b");
        check(!f.isNotificationEnabled(new Notification("a.b", "src", 1L)), "disabled after disableType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
