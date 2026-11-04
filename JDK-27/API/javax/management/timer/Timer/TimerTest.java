/*
 * @test
 * @summary javax.management.timer.Timer conformance: white-box structure + black-box behavior.
 * @run main TimerTest
 */

import java.util.Collections;
import java.util.Date;
import java.util.Vector;
import javax.management.InstanceNotFoundException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.timer.Timer;
import javax.management.timer.TimerMBean;

public class TimerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Vector<Integer> sorted(Vector<Integer> v) {
        Vector<Integer> c = new Vector<>(v);
        Collections.sort(c);
        return c;
    }
    // A fixed date in the future (2100-01-01T00:00:00Z) so addNotification does
    // not clamp it to the wall clock and getDate stays deterministic.
    static final long FUTURE_MILLIS = 4102444800000L;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.timer.Timer");
        check(!c.isInterface(), "not interface");
        check(NotificationBroadcasterSupport.class.isAssignableFrom(c),
                "extends NotificationBroadcasterSupport");
        check(TimerMBean.class.isAssignableFrom(c), "implements TimerMBean");
        check(javax.management.MBeanRegistration.class.isAssignableFrom(c), "implements MBeanRegistration");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Timer t = new Timer();
        check(t.isEmpty(), "initially empty");
        check(t.getNbNotifications() == 0, "zero notifications");
        check(!t.isActive(), "not active");
        check(!t.getSendPastNotifications(), "default sendPastNotifications false");

        Date future = new Date(FUTURE_MILLIS);

        Integer id1 = t.addNotification("typeA", "msgA", "userA", future);
        check(id1 != null, "once id non-null");
        check(t.getNbNotifications() == 1, "one notification");

        Integer id2 = t.addNotification("typeB", "msgB", "userB", future, 500L);
        Integer id3 = t.addNotification("typeA", "msgC", "userC", future, 1000L, 5L);
        Integer id4 = t.addNotification("typeA", "msgD", "userD", future, 2000L, 3L, true);
        check(t.getNbNotifications() == 4, "four notifications");

        // IDs are distinct.
        check(!id1.equals(id2) && !id2.equals(id3) && !id3.equals(id4), "ids distinct");

        // getAllNotificationIDs (sort before comparing)
        Vector<Integer> all = sorted(t.getAllNotificationIDs());
        check(all.size() == 4, "all ids size 4");
        check(all.contains(id1) && all.contains(id2) && all.contains(id3) && all.contains(id4),
                "all ids contains every id");

        // getNotificationIDs by type
        Vector<Integer> typeA = sorted(t.getNotificationIDs("typeA"));
        check(typeA.size() == 3 && typeA.contains(id1) && typeA.contains(id3) && typeA.contains(id4),
                "typeA ids");
        Vector<Integer> typeB = sorted(t.getNotificationIDs("typeB"));
        check(typeB.size() == 1 && typeB.contains(id2), "typeB ids");
        check(t.getNotificationIDs("missing").isEmpty(), "no ids for missing type");

        // per-notification queries
        check("typeA".equals(t.getNotificationType(id1)), "notification type");
        check("msgA".equals(t.getNotificationMessage(id1)), "notification message");
        check("userA".equals(t.getNotificationUserData(id1)), "notification user data");
        check(new Date(FUTURE_MILLIS).equals(t.getDate(id1)), "once date unchanged (future)");
        check(t.getPeriod(id1).longValue() == 0L, "once period 0");
        check(t.getNbOccurences(id1).longValue() == 0L, "once occurrences 0");
        check(!t.getFixedRate(id1).booleanValue(), "once fixed rate false");

        check(t.getPeriod(id2).longValue() == 500L, "period 500");
        check(t.getPeriod(id3).longValue() == 1000L, "period 1000");
        check(t.getNbOccurences(id3).longValue() == 5L, "occurrences 5");
        check(!t.getFixedRate(id3).booleanValue(), "fixed rate false for id3");
        check(t.getPeriod(id4).longValue() == 2000L, "period 2000");
        check(t.getNbOccurences(id4).longValue() == 3L, "occurrences 3");
        check(t.getFixedRate(id4).booleanValue(), "fixed rate true for id4");

        // missing id queries return null
        check(t.getNotificationType(Integer.valueOf(-999)) == null, "missing type null");

        // setSendPastNotifications
        t.setSendPastNotifications(false);
        check(!t.getSendPastNotifications(), "sendPastNotifications false");
        t.setSendPastNotifications(true);
        check(t.getSendPastNotifications(), "sendPastNotifications true");

        // removeNotification
        t.removeNotification(id1);
        check(t.getNbNotifications() == 3, "three after removeNotification");
        boolean nfe1 = false;
        try { t.removeNotification(id1); } catch (InstanceNotFoundException e) { nfe1 = true; }
        check(nfe1, "removeNotification missing throws InstanceNotFoundException");

        // removeNotifications(type)
        t.removeNotifications("typeB");
        check(t.getNbNotifications() == 2, "two after removeNotifications(typeB)");
        boolean nfe2 = false;
        try { t.removeNotifications("typeB"); } catch (InstanceNotFoundException e) { nfe2 = true; }
        check(nfe2, "removeNotifications missing type throws InstanceNotFoundException");

        // removeAllNotifications
        t.removeAllNotifications();
        check(t.isEmpty(), "empty after removeAllNotifications");
        check(t.getNbNotifications() == 0, "zero after removeAllNotifications");

        // validation
        boolean iae1 = false;
        try { t.addNotification("t", "m", "u", null); } catch (IllegalArgumentException e) { iae1 = true; }
        check(iae1, "addNotification null date throws IllegalArgumentException");
        boolean iae2 = false;
        try { t.addNotification("t", "m", "u", future, -1L); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "addNotification negative period throws IllegalArgumentException");

        // constants
        check(Timer.ONE_SECOND == 1000L, "ONE_SECOND");
        check(Timer.ONE_MINUTE == 60000L, "ONE_MINUTE");
        check(Timer.ONE_HOUR == 3600000L, "ONE_HOUR");
        check(Timer.ONE_DAY == 86400000L, "ONE_DAY");
        check(Timer.ONE_WEEK == 604800000L, "ONE_WEEK");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
