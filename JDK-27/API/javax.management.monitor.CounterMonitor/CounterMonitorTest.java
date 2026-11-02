/*
 * @test
 * @summary javax.management.monitor.CounterMonitor conformance: JMX API type verification.
 * @run main CounterMonitorTest
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

public class CounterMonitorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        CounterMonitor cm = new CounterMonitor();
        cm.setInitThreshold(5);
        check(cm.getInitThreshold().intValue() == 5, "init threshold");
        cm.setModulus(100);
        check(cm.getModulus().intValue() == 100, "modulus");
        cm.setOffset(2);
        check(cm.getOffset().intValue() == 2, "offset");
        cm.setNotify(true);
        check(cm.getNotify(), "notify");
        cm.setDifferenceMode(true);
        check(cm.getDifferenceMode(), "difference mode");
        check(cm.getNotificationInfo().length > 0, "notif info");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
