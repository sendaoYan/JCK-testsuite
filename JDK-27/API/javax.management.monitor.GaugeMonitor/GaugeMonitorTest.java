/*
 * @test
 * @summary javax.management.monitor.GaugeMonitor conformance: JMX API type verification.
 * @run main GaugeMonitorTest
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

public class GaugeMonitorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        GaugeMonitor gm = new GaugeMonitor();
        gm.setThresholds(100, 0);
        check(gm.getHighThreshold().intValue() == 100, "high threshold");
        check(gm.getLowThreshold().intValue() == 0, "low threshold");
        gm.setNotifyHigh(true);
        gm.setNotifyLow(true);
        check(gm.getNotifyHigh() && gm.getNotifyLow(), "notify flags");
        gm.setDifferenceMode(true);
        check(gm.getDifferenceMode(), "difference mode");
        check(gm.getNotificationInfo().length > 0, "notif info");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
