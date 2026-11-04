/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerBuilder conformance: white-box structure + black-box behavior.
 * @run main MBeanServerBuilderTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanServerBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
    }
    public static class Thing implements ThingMBean {
        public int getCount() { return 7; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerBuilder");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(hasMethod(c, "newMBeanServerDelegate"), "has newMBeanServerDelegate");
        check(hasMethod(c, "newMBeanServer"), "has newMBeanServer");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanServerBuilder builder = new MBeanServerBuilder();

        MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
        check(delegate != null, "newMBeanServerDelegate non-null");
        check(delegate.getMBeanServerId() != null, "delegate id non-null");

        MBeanServer server = builder.newMBeanServer("built.domain", null, delegate);
        check(server != null, "newMBeanServer non-null");
        check("built.domain".equals(server.getDefaultDomain()), "default domain set");
        check(server.getMBeanCount() == 1, "fresh server has only the delegate MBean");

        // the constructed server is a working MBeanServer
        ObjectName on = new ObjectName("built.domain:type=Thing");
        server.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        check(server.isRegistered(on), "register works on built server");
        check(server.getAttribute(on, "Count").equals(7), "getAttribute works");
        server.unregisterMBean(on);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepSubclassBuilder();
        deepDefaultBuilderSemantics();

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

    // ==== appended deepening (2026-11): builder subclass + closed-loop type ====

    public static class RecordingBuilder extends javax.management.MBeanServerBuilder {
        int delegateCalls;
        javax.management.MBeanServerDelegate lastDelegate;
        public javax.management.MBeanServerDelegate newMBeanServerDelegate() {
            delegateCalls++;
            lastDelegate = new javax.management.MBeanServerDelegate();
            return lastDelegate;
        }
    }

    public static interface WidgetMBean {
        int getSize();
        void setSize(int s);
    }

    public static class Widget implements WidgetMBean {
        private int size = 5;
        public int getSize() { return size; }
        public void setSize(int s) { size = s; }
    }

    // ==== appended deepening (2026-11): subclass overrides newMBeanServerDelegate ====

    static void deepSubclassBuilder() throws Exception {
        RecordingBuilder rb = new RecordingBuilder();
        javax.management.MBeanServerDelegate d = rb.newMBeanServerDelegate();
        check(rb.delegateCalls == 1, "overridden newMBeanServerDelegate invoked");
        check(rb.lastDelegate == d, "recorded delegate is the returned instance");
        check(d.getMBeanServerId() != null, "subclass-produced delegate functional");
        javax.management.MBeanServer s = rb.newMBeanServer("jck.bld", null, d);
        check("jck.bld".equals(s.getDefaultDomain()), "builder default domain used");
        check(s.getMBeanCount().intValue() == 1, "fresh built server contains only the delegate");
        check(s.isRegistered(javax.management.MBeanServerDelegate.DELEGATE_NAME), "supplied delegate registered");
        String idViaServer = (String) s.getAttribute(javax.management.MBeanServerDelegate.DELEGATE_NAME, "MBeanServerId");
        check(d.getMBeanServerId().equals(idViaServer), "server exposes the supplied delegate instance");
        javax.management.ObjectName on = new javax.management.ObjectName("jck.bld:type=Widget");
        s.registerMBean(new Widget(), on);
        check(Integer.valueOf(5).equals(s.getAttribute(on, "Size")), "built server getAttribute");
        s.setAttribute(on, new javax.management.Attribute("Size", Integer.valueOf(9)));
        check(Integer.valueOf(9).equals(s.getAttribute(on, "Size")), "built server setAttribute");
        check(s.isInstanceOf(on, Widget.class.getName()), "built server isInstanceOf");
        s.unregisterMBean(on);
        check(!s.isRegistered(on), "built server unregisterMBean");
        check(rb.delegateCalls == 1, "newMBeanServer(4-arg) does not call newMBeanServerDelegate");
    }

    // ==== appended deepening (2026-11): default builder semantics ====

    static void deepDefaultBuilderSemantics() throws Exception {
        javax.management.MBeanServerBuilder plain = new javax.management.MBeanServerBuilder();
        check(plain.newMBeanServerDelegate() instanceof javax.management.MBeanServerDelegate,
                "default builder produces MBeanServerDelegate");
        check(plain.newMBeanServerDelegate().getMBeanServerId() != null, "default delegate has an id");
        javax.management.MBeanServer nullDom = plain.newMBeanServer(null, null, plain.newMBeanServerDelegate());
        check("DefaultDomain".equals(nullDom.getDefaultDomain()), "null defaultDomain -> DefaultDomain");
        check(nullDom.getMBeanCount().intValue() == 1, "null-domain server is alive");
        javax.management.MBeanServer nullDel = plain.newMBeanServer("d2", null, null);
        check(nullDel.getMBeanCount().intValue() == 1
                && nullDel.isRegistered(javax.management.MBeanServerDelegate.DELEGATE_NAME),
                "null delegate still yields a working server with a delegate");
    }
}

