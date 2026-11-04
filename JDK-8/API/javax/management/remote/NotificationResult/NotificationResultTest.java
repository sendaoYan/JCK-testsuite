/*
 * @test
 * @summary javax.management.remote.NotificationResult conformance: sequence numbers and notifications.
 * @run main NotificationResultTest
 */

import javax.management.*;
import javax.management.remote.*;
public class NotificationResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        TargetedNotification[] tns = new TargetedNotification[]{
            new TargetedNotification(new Notification("t", "src", 1L), Integer.valueOf(1))
        };
        NotificationResult nr = new NotificationResult(1L, 2L, tns);
        check(nr.getEarliestSequenceNumber() == 1L, "earliest");
        check(nr.getNextSequenceNumber() == 2L, "next");
        check(nr.getTargetedNotifications().length == 1, "notifications");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
