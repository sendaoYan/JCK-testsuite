/*
 * @test
 * @summary javax.management.timer.TimerMBean conformance: white-box structure + black-box behavior.
 * @run main TimerMBeanTest
 */

import java.lang.reflect.Method;
import java.util.Date;
import javax.management.timer.Timer;
import javax.management.timer.TimerMBean;

public class TimerMBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerMBean");
        check(c.isInterface(), "is interface");
        Method[] mm = c.getMethods();
        check(has(mm, "start"), "has start");
        check(has(mm, "stop"), "has stop");
        check(has(mm, "addNotification"), "has addNotification");
        check(has(mm, "removeNotification"), "has removeNotification");
        check(has(mm, "removeNotifications"), "has removeNotifications");
        check(has(mm, "removeAllNotifications"), "has removeAllNotifications");
        check(has(mm, "getNbNotifications"), "has getNbNotifications");
        check(has(mm, "getAllNotificationIDs"), "has getAllNotificationIDs");
        check(has(mm, "getNotificationIDs"), "has getNotificationIDs");
        check(has(mm, "getNotificationType"), "has getNotificationType");
        check(has(mm, "getNotificationMessage"), "has getNotificationMessage");
        check(has(mm, "getNotificationUserData"), "has getNotificationUserData");
        check(has(mm, "getDate"), "has getDate");
        check(has(mm, "getPeriod"), "has getPeriod");
        check(has(mm, "getNbOccurences"), "has getNbOccurences");
        check(has(mm, "getFixedRate"), "has getFixedRate");
        check(has(mm, "getSendPastNotifications"), "has getSendPastNotifications");
        check(has(mm, "setSendPastNotifications"), "has setSendPastNotifications");
        check(has(mm, "isActive"), "has isActive");
        check(has(mm, "isEmpty"), "has isEmpty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TimerMBean t = new Timer();
        Integer id = t.addNotification("type", "msg", "ud", new Date(4102444800000L));
        check(t.getNbNotifications() == 1, "interface getNbNotifications");
        check(t.getAllNotificationIDs().contains(id), "interface getAllNotificationIDs");
        check("type".equals(t.getNotificationType(id)), "interface getNotificationType");
        check(t.isEmpty() == false, "interface isEmpty");
        t.removeNotification(id);
        check(t.isEmpty(), "interface isEmpty after remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
