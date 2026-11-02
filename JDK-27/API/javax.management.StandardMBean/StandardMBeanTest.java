/*
 * @test
 * @summary javax.management.StandardMBean conformance: JMX API type verification.
 * @run main StandardMBeanTest
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

public class StandardMBeanTest {
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
        StandardMBean smb = new StandardMBean(new Thing(), ThingMBean.class);
        check(smb.getMBeanInterface() == ThingMBean.class, "interface");
        check(smb.getImplementation() instanceof Thing, "implementation");
        MBeanInfo info = smb.getMBeanInfo();
        check(info.getAttributes().length == 1, "one attribute");
        check(info.getAttributes()[0].getName().equals("Count"), "attr name");
        smb.setAttribute(new Attribute("Count", 42));
        check(((Integer) smb.getAttribute("Count")).intValue() == 42, "set/get");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("test:type=StandardMBeanTest");
        mbs.registerMBean(smb, on);
        check(mbs.isRegistered(on), "registered");
        check(((Integer) mbs.getAttribute(on, "Count")).intValue() == 42, "mbs get");
        mbs.unregisterMBean(on);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
