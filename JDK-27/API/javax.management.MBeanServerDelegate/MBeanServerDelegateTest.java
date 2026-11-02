/*
 * @test
 * @summary javax.management.MBeanServerDelegate conformance: JMX API type verification.
 * @run main MBeanServerDelegateTest
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

public class MBeanServerDelegateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanServerDelegate d = new MBeanServerDelegate();
        check(d.getMBeanServerId() != null, "mbeanServerId");
        check(d.getSpecificationName() != null, "spec name");
        check(d.getSpecificationVersion() != null, "spec version");
        check(d.getSpecificationVendor() != null, "spec vendor");
        check(d.getImplementationName() != null, "impl name");
        check(d.getImplementationVersion() != null, "impl version");
        check(d.getImplementationVendor() != null, "impl vendor");
        check(d.getNotificationInfo().length >= 1, "notif info");
        check(MBeanServerDelegate.DELEGATE_NAME != null, "delegate name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
