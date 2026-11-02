/*
 * @test
 * @summary javax.management.relation.MBeanServerNotificationFilter conformance: JMX API type verification.
 * @run main MBeanServerNotificationFilterTest
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

public class MBeanServerNotificationFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
        ObjectName on = new ObjectName("d:type=x");
        f.enableObjectName(on);
        check(f.getEnabledObjectNames().size() == 1, "enabled size");
        MBeanServerNotification n = new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, "s", 1L, on);
        check(f.isNotificationEnabled(n), "enabled object");
        check(!f.isNotificationEnabled(new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, "s", 1L, new ObjectName("d:type=y"))), "not enabled");
        f.disableObjectName(on);
        check(f.getEnabledObjectNames().size() == 0, "disabled");
        f.enableAllObjectNames();
        check(f.getDisabledObjectNames().size() == 0, "enable all");
        f.disableAllObjectNames();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
