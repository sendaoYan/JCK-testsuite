/*
 * @test
 * @summary javax.management.JMX conformance: JMX API type verification.
 * @run main JMXTest
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

public class JMXTest {
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
        check(!JMX.isMXBeanInterface(ThingMBean.class), "not mxbean iface");
        check(JMX.DEFAULT_VALUE_FIELD != null, "DEFAULT_VALUE_FIELD");
        check(JMX.IMMUTABLE_INFO_FIELD != null, "IMMUTABLE_INFO_FIELD");
        check(JMX.MXBEAN_FIELD != null, "MXBEAN_FIELD");
        check(JMX.OPEN_TYPE_FIELD != null, "OPEN_TYPE_FIELD");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("test:type=JMXTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        ThingMBean p = JMX.newMBeanProxy(mbs, on, ThingMBean.class);
        check(p.getCount() == 7, "proxy count");
        p.setCount(100);
        check(p.getCount() == 100, "proxy set");
        mbs.unregisterMBean(on);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
