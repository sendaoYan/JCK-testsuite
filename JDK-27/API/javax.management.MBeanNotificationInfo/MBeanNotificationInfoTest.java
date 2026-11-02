/*
 * @test
 * @summary javax.management.MBeanNotificationInfo conformance: JMX API type verification.
 * @run main MBeanNotificationInfoTest
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

public class MBeanNotificationInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanNotificationInfo ni = new MBeanNotificationInfo(new String[]{"a.b", "c.d"}, "javax.management.Notification", "desc");
        check(ni.getName().equals("javax.management.Notification"), "name");
        check(ni.getDescription().equals("desc"), "description");
        check(ni.getNotifTypes().length == 2, "notif types");
        check(ni.getNotifTypes()[0].equals("a.b"), "notif type 0");
        MBeanNotificationInfo c = (MBeanNotificationInfo) ni.clone();
        check(c.equals(ni), "clone equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
