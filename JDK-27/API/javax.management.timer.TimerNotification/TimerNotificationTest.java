/*
 * @test
 * @summary javax.management.timer.TimerNotification conformance: white-box structure + black-box behavior.
 * @run main TimerNotificationTest
 */

import javax.management.Notification;
import javax.management.timer.TimerNotification;

public class TimerNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerNotification");
        check(Notification.class.isAssignableFrom(c), "extends Notification");
        check(c.getConstructors().length == 1, "single public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object source = new Object();
        TimerNotification n = new TimerNotification("type", source, 1L, 100L, "msg", Integer.valueOf(42));
        check("type".equals(n.getType()), "getType inherited");
        check(n.getSequenceNumber() == 1L, "getSequenceNumber inherited");
        check(n.getTimeStamp() == 100L, "getTimeStamp inherited");
        check("msg".equals(n.getMessage()), "getMessage inherited");
        check(source == n.getSource(), "getSource inherited");
        check(Integer.valueOf(42).equals(n.getNotificationID()), "getNotificationID");

        // userData is inherited from Notification and settable.
        n.setUserData("payload");
        check("payload".equals(n.getUserData()), "setUserData/getUserData");

        // A null id is representable.
        TimerNotification n2 = new TimerNotification("t2", source, 2L, 200L, "m2", null);
        check(n2.getNotificationID() == null, "null notification id preserved");
        check("t2".equals(n2.getType()), "second type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
