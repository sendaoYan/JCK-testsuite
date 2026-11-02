/*
 * @test
 * @summary javax.management.timer.Timer conformance: JMX API type verification.
 * @run main TimerTest
 */

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.security.*;
import java.rmi.*;
import java.rmi.server.*;
import javax.security.auth.Subject;
import javax.management.*;
import javax.management.loading.*;
import javax.management.modelmbean.*;
import javax.management.monitor.*;
import javax.management.openmbean.*;
import javax.management.relation.*;
import javax.management.remote.*;
import javax.management.remote.rmi.*;
import javax.management.timer.*;

public class TimerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Timer t = new Timer();
        check(t.isEmpty(), "empty");
        check(t.getNbNotifications() == 0, "zero notif");
        check(!t.isActive(), "not active");
        Date d = new Date(System.currentTimeMillis() + 60000);
        Integer id = t.addNotification("type", "msg", "userData", d);
        check(id != null, "id non-null");
        check(t.getNbNotifications() == 1, "one notif");
        check(t.getAllNotificationIDs().size() == 1, "all ids");
        check(t.getNotificationType(id).equals("type"), "type");
        check(t.getNotificationMessage(id).equals("msg"), "message");
        check(t.getNotificationUserData(id).equals("userData"), "user data");
        check(t.getDate(id).equals(d), "date");
        check(t.getPeriod(id).longValue() == 0, "period");
        check(t.getNbOccurences(id).longValue() == 0, "occurrences");
        check(t.getFixedRate(id).booleanValue() == false, "fixed rate");
        t.setSendPastNotifications(false);
        check(!t.getSendPastNotifications(), "send past");
        t.removeNotification(id);
        check(t.isEmpty(), "empty after remove");
        check(Timer.ONE_SECOND == 1000L && Timer.ONE_MINUTE == 60000L, "constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
