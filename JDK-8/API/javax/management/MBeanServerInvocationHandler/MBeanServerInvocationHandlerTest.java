/*
 * @test
 * @summary javax.management.MBeanServerInvocationHandler conformance: proxy calls through to MBean.
 * @run main MBeanServerInvocationHandlerTest
 */

import java.lang.management.*;
import javax.management.*;

public class MBeanServerInvocationHandlerTest {
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
        Counter impl = new Counter();
        StandardMBean smb = new StandardMBean(impl, CounterMBean.class);
        ObjectName name = new ObjectName("conformance:type=CounterProxy");
        mbs.registerMBean(smb, name);
        CounterMBean proxy = MBeanServerInvocationHandler.newProxyInstance(mbs, name, CounterMBean.class, false);
        proxy.setCount(7);
        check(proxy.getCount() == 7, "proxy get");
        check(proxy.increment() == 8, "proxy invoke");
        mbs.unregisterMBean(name);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
