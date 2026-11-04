/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerInvocationHandler conformance: proxy calls through to MBean.
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
        deepProxyRoundTrip();
        deepHandlerAccessors();
        deepProxyFailures();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): shared helpers ====

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ==== appended deepening (2026-11): closed-loop types ====

    public static interface MeterMBean {
        int getCount();
        void setCount(int v);
        boolean isArmed();
        void reset();
        int add(int x);
        void detonate() throws Exception;
    }

    public static class Meter implements MeterMBean {
        static final Exception BOOM = new Exception("meter-boom");
        private int count = 3;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public boolean isArmed() { return true; }
        public void reset() { count = 0; }
        public int add(int x) { return count + x; }
        public void detonate() throws Exception { throw BOOM; }
    }

    public static interface GhostMeterMBean {
        int getCount();
        int getPhantom();
    }

    // ==== appended deepening (2026-11): proxy round-trip on a local server ====

    static void deepProxyRoundTrip() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.px:type=Meter");
        final Meter impl = new Meter();
        server.registerMBean(impl, on);
        MeterMBean proxy = javax.management.MBeanServerInvocationHandler.newProxyInstance(
                server, on, MeterMBean.class, false);
        check(proxy instanceof MeterMBean, "proxy implements the interface");
        check(proxy.getCount() == 3, "proxy getter reads the real value");
        proxy.setCount(9);
        check(Integer.valueOf(9).equals(server.getAttribute(on, "Count")), "proxy setter reaches the server");
        check(impl.getCount() == 9, "proxy setter reaches the implementation");
        check(proxy.getCount() == 9, "proxy read-back after set");
        check(proxy.isArmed(), "proxy boolean is-getter");
        check(proxy.add(5) == 14, "proxy operation add(5)");
        check(Integer.valueOf(9).equals(server.getAttribute(on, "Count")), "pure operation leaves state");
        proxy.reset();
        check(impl.getCount() == 0, "proxy void operation reaches the implementation");
        check(proxy.getCount() == 0, "state visible through the proxy after reset");
        // Object methods are answered by the proxy itself, not routed to the server
        check(proxy.equals(proxy), "proxy equals itself");
        check(!proxy.equals(new Meter()), "proxy not equal to an unrelated object");
        check(proxy.hashCode() == proxy.hashCode(), "proxy hashCode stable across calls");
        check(proxy.toString().contains("jck.px:type=Meter"), "proxy toString mentions the ObjectName");
    }

    // ==== appended deepening (2026-11): handler constructors and direct invoke ====

    static void deepHandlerAccessors() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.px:type=Meter");
        server.registerMBean(new Meter(), on);
        javax.management.MBeanServerInvocationHandler h =
                new javax.management.MBeanServerInvocationHandler(server, on);
        check(h.getMBeanServerConnection() == server, "getMBeanServerConnection is the given server");
        check(on.equals(h.getObjectName()), "getObjectName exact");
        check(!h.isMXBean(), "2-arg constructor defaults to standard MBean");
        javax.management.MBeanServerInvocationHandler mx =
                new javax.management.MBeanServerInvocationHandler(server, on, true);
        check(mx.isMXBean(), "3-arg constructor with true is an MXBean handler");
        // direct InvocationHandler.invoke with reflected Method objects
        final MeterMBean proxy = javax.management.MBeanServerInvocationHandler.newProxyInstance(
                server, on, MeterMBean.class, false);
        Object got;
        Object added;
        try {
            got = h.invoke(proxy, MeterMBean.class.getMethod("getCount"), null);
            added = h.invoke(proxy, MeterMBean.class.getMethod("add", int.class),
                    new Object[]{Integer.valueOf(6)});
        } catch (Throwable t) {
            throw new AssertionError("direct InvocationHandler.invoke failed", t);
        }
        check(got instanceof Integer && ((Integer) got).intValue() == 3, "direct invoke getCount");
        check(added instanceof Integer && ((Integer) added).intValue() == 9, "direct invoke add(6)");
    }

    // ==== appended deepening (2026-11): failure modes ====

    static void deepProxyFailures() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.px:type=Meter");
        server.registerMBean(new Meter(), on);
        // target not registered
        MeterMBean dead = javax.management.MBeanServerInvocationHandler.newProxyInstance(
                server, new javax.management.ObjectName("jck.px:type=Missing"), MeterMBean.class, false);
        try {
            dead.getCount();
            check(false, "dead proxy must throw");
        } catch (java.lang.reflect.UndeclaredThrowableException e) {
            check(e.getCause() instanceof javax.management.InstanceNotFoundException,
                    "dead proxy wraps InstanceNotFoundException");
        }
        // proxy interface wider than the MBean
        GhostMeterMBean g = javax.management.MBeanServerInvocationHandler.newProxyInstance(
                server, on, GhostMeterMBean.class, false);
        check(g.getCount() == 3, "narrowed proxy common getter still works");
        try {
            g.getPhantom();
            check(false, "phantom getter must throw");
        } catch (java.lang.reflect.UndeclaredThrowableException e) {
            check(e.getCause() instanceof javax.management.AttributeNotFoundException,
                    "phantom getter wraps AttributeNotFoundException");
        }
        // business exception: the server wraps it in MBeanException with the exact cause...
        try {
            server.invoke(on, "detonate", new Object[0], new String[0]);
            check(false, "server-level detonate must throw");
        } catch (javax.management.MBeanException e) {
            check(e.getCause() == Meter.BOOM, "server-level MBeanException cause is the exact instance");
            check(e.getTargetException() == Meter.BOOM, "server-level getTargetException identical");
        }
        // ...while the proxy rethrows the original instance as-is because the
        // interface method declares it in its throws clause
        MeterMBean px = javax.management.MBeanServerInvocationHandler.newProxyInstance(
                server, on, MeterMBean.class, false);
        try {
            px.detonate();
            check(false, "proxy detonate must throw");
        } catch (javax.management.MBeanException e) {
            check(false, "proxy must not rewrap a declared exception: " + e);
        } catch (Exception e) {
            check(e == Meter.BOOM, "proxy rethrows the original exception instance unchanged");
        }
    }
}

