/*
 * @test
 * @summary javax.management.MBeanServer conformance: register/getAttribute/setAttribute/invoke via ManagementFactory.
 * @run main MBeanServerTest
 */

import java.lang.management.*;
import javax.management.*;

public class MBeanServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public interface CounterMBean { int getCount(); void setCount(int v); int increment(); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public int increment() { return ++count; }
    }

    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        check(mbs != null, "platform MBeanServer");
        Counter impl = new Counter();
        StandardMBean smb = new StandardMBean(impl, CounterMBean.class);
        ObjectName name = new ObjectName("conformance:type=Counter");
        mbs.registerMBean(smb, name);
        check(mbs.isRegistered(name), "isRegistered");
        impl.setCount(5);
        check(((Integer) mbs.getAttribute(name, "Count")).intValue() == 5, "getAttribute");
        mbs.setAttribute(name, new Attribute("Count", Integer.valueOf(10)));
        check(impl.getCount() == 10, "setAttribute");
        check(((Integer) mbs.invoke(name, "increment", new Object[0], new String[0])).intValue() == 11, "invoke");
        MBeanInfo info = mbs.getMBeanInfo(name);
        check(Counter.class.getName().equals(info.getClassName()), "MBeanInfo class name");
        check(mbs.queryNames(new ObjectName("conformance:type=*"), null).contains(name), "queryNames");
        mbs.unregisterMBean(name);
        check(!mbs.isRegistered(name), "unregistered");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
