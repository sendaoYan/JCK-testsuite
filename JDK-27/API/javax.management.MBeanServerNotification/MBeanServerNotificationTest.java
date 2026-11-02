/*
 * @test
 * @summary javax.management.MBeanServerNotification conformance: JMX API type verification.
 * @run main MBeanServerNotificationTest
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

public class MBeanServerNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        ObjectName on = new ObjectName("domain:type=x");
        MBeanServerNotification n = new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, "src", 1L, on);
        check(n.getMBeanName().equals(on), "mbean name");
        check(n.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION), "type");
        check(MBeanServerNotification.UNREGISTRATION_NOTIFICATION != null, "unregistration const");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
