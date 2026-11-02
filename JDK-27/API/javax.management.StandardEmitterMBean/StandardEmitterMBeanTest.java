/*
 * @test
 * @summary javax.management.StandardEmitterMBean conformance: JMX API type verification.
 * @run main StandardEmitterMBeanTest
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

public class StandardEmitterMBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public interface ThingMBean {
        int getCount();
        void setCount(int c);
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
    }

    public static void main(String[] args) throws Exception {
        StandardEmitterMBean emb = new StandardEmitterMBean(new Thing(), ThingMBean.class, new NotificationBroadcasterSupport());
        check(emb.getMBeanInterface() == ThingMBean.class, "interface");
        check(emb.getImplementation() instanceof Thing, "implementation");
        check(emb.getNotificationInfo().length == 0, "no notif info");
        emb.sendNotification(new Notification("type", emb, 1L));
        MBeanInfo info = emb.getMBeanInfo();
        check(info.getAttributes().length == 1, "one attribute");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("test:type=StandardEmitterMBeanTest");
        mbs.registerMBean(emb, on);
        check(mbs.isRegistered(on), "registered");
        mbs.unregisterMBean(on);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
