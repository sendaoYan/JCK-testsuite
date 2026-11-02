/*
 * @test
 * @summary javax.management.MBeanInfo conformance: JMX API type verification.
 * @run main MBeanInfoTest
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

public class MBeanInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanInfo mi = new MBeanInfo("java.lang.String", "desc",
            new MBeanAttributeInfo[0], new MBeanConstructorInfo[0],
            new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
        check(mi.getClassName().equals("java.lang.String"), "className");
        check(mi.getDescription().equals("desc"), "description");
        check(mi.getAttributes().length == 0, "attributes");
        check(mi.getConstructors().length == 0, "constructors");
        check(mi.getOperations().length == 0, "operations");
        check(mi.getNotifications().length == 0, "notifications");
        MBeanInfo c = (MBeanInfo) mi.clone();
        check(c.equals(mi), "clone equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
