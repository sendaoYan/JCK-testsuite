/*
 * @test
 * @summary javax.management.remote.TargetedNotification conformance: notification + listener id.
 * @run main TargetedNotificationTest
 */

import javax.management.*;
import javax.management.remote.*;
public class TargetedNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Notification n = new Notification("t", "src", 1L);
        TargetedNotification tn = new TargetedNotification(n, Integer.valueOf(5));
        check(tn.getNotification() == n, "notification");
        check(Integer.valueOf(5).equals(tn.getListenerID()), "listener id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
