/*
 * @test
 * @summary javax.management.NotificationBroadcasterSupport conformance: JMX API type verification.
 * @run main NotificationBroadcasterSupportTest
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

public class NotificationBroadcasterSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        final Notification[] got = new Notification[1];
        NotificationListener l = (notif, handback) -> { got[0] = notif; };
        nbs.addNotificationListener(l, null, null);
        Notification n = new Notification("type", nbs, 1L);
        nbs.sendNotification(n);
        check(got[0] == n, "received");
        nbs.removeNotificationListener(l);
        check(nbs.getNotificationInfo().length == 0, "no info");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
