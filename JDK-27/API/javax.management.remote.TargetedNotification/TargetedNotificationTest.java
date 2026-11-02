/*
 * @test
 * @summary javax.management.remote.TargetedNotification conformance: JMX API type verification.
 * @run main TargetedNotificationTest
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

public class TargetedNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        TargetedNotification tn = new TargetedNotification(new Notification("t", "s", 1L), 5);
        check(tn.getListenerID().intValue() == 5, "listener id");
        check(tn.getNotification().getType().equals("t"), "notification");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
