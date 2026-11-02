/*
 * @test
 * @summary javax.management.MBeanServerInvocationHandler conformance: JMX API type verification.
 * @run main MBeanServerInvocationHandlerTest
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

public class MBeanServerInvocationHandlerTest {
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
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("test:type=MBeanServerInvocationHandlerTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        ThingMBean proxy = MBeanServerInvocationHandler.newProxyInstance(mbs, on, ThingMBean.class, false);
        check(proxy.getCount() == 7, "proxy count");
        proxy.setCount(99);
        check(proxy.getCount() == 99, "proxy set");
        MBeanServerInvocationHandler h = new MBeanServerInvocationHandler(mbs, on);
        check(h.getMBeanServerConnection() == mbs, "connection");
        check(h.getObjectName().equals(on), "objectName");
        check(!h.isMXBean(), "not mxbean");
        mbs.unregisterMBean(on);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
