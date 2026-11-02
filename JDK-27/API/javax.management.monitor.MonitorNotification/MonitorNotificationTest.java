/*
 * @test
 * @summary javax.management.monitor.MonitorNotification conformance: JMX API type verification.
 * @run main MonitorNotificationTest
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

public class MonitorNotificationTest {
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
        Class<?> c = Class.forName("javax.management.monitor.MonitorNotification");
        check(Notification.class.isAssignableFrom(c), "extends Notification");
        check(c.getField("THRESHOLD_VALUE_EXCEEDED") != null, "has THRESHOLD_VALUE_EXCEEDED");
        check(c.getField("OBSERVED_OBJECT_ERROR") != null, "has OBSERVED_OBJECT_ERROR");
        check(c.getField("STRING_TO_COMPARE_VALUE_MATCHED") != null, "has STRING_TO_COMPARE_VALUE_MATCHED");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "getObservedObject"), "getObservedObject");
        check(has(mm, "getObservedAttribute"), "getObservedAttribute");
        check(has(mm, "getDerivedGauge"), "getDerivedGauge");
        check(has(mm, "getTrigger"), "getTrigger");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
