/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerBuilder conformance: concrete class reflection.
 * @run main MBeanServerBuilderTest
 */

public class MBeanServerBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerBuilder");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "newMBeanServer"), "declares newMBeanServer");
        check(hasMethod(c, "newMBeanServerDelegate"), "declares newMBeanServerDelegate");
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

