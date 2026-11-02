/*
 * @test
 * @summary javax.management.NotificationBroadcasterSupport conformance: listener registration and send.
 * @run main NotificationBroadcasterSupportTest
 */

import javax.management.*;
public class NotificationBroadcasterSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        final Notification[] received = new Notification[1];
        nbs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { received[0] = n; }
        }, null, null);
        Notification n = new Notification("t", "src", 1L);
        nbs.sendNotification(n);
        check(received[0] == n, "listener received");
        check(nbs.getNotificationInfo() != null, "getNotificationInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
