/*
 * @test
 * @summary javax.management.timer.TimerMBean conformance: JMX API type verification.
 * @run main TimerMBeanTest
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

public class TimerMBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerMBean");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Method[] mm = c.getMethods();
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
