/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerInvocationHandler conformance: white-box structure + black-box behavior.
 * @run main MBeanServerInvocationHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class MBeanServerInvocationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerInvocationHandler");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(InvocationHandler.class.isAssignableFrom(c), "implements InvocationHandler");
        check(hasMethod(c, "getMBeanServerConnection"), "has getMBeanServerConnection");
        check(hasMethod(c, "getObjectName"), "has getObjectName");
        check(hasMethod(c, "isMXBean"), "has isMXBean");
        check(hasMethod(c, "newProxyInstance"), "has static newProxyInstance");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=MBeanServerInvocationHandlerTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        try {
            // constructor + accessors
            MBeanServerInvocationHandler handler = new MBeanServerInvocationHandler(mbs, on);
            check(handler.getMBeanServerConnection() == mbs, "getMBeanServerConnection");
            check(handler.getObjectName().equals(on), "getObjectName");
            check(!handler.isMXBean(), "isMXBean false for standard MBean");

            MBeanServerInvocationHandler mxHandler = new MBeanServerInvocationHandler(mbs, on, true);
            check(mxHandler.isMXBean(), "isMXBean true when requested");

            // static newProxyInstance produces a working proxy
            ThingMBean proxy = MBeanServerInvocationHandler.newProxyInstance(mbs, on, ThingMBean.class, false);
            check(proxy.getCount() == 7, "proxy getCount");
            proxy.setCount(50);
            check(proxy.getCount() == 50, "proxy setCount round-trip");

            // direct invoke() through InvocationHandler contract
            Method getCount = ThingMBean.class.getMethod("getCount");
            Object result;
            try {
                result = handler.invoke(proxy, getCount, null);
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
            check(((Integer) result).intValue() == 50, "invoke via handler");
        } finally {
            mbs.unregisterMBean(on);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

