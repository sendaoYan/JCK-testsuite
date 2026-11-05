/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectorServer conformance: white-box structure + black-box behavior.
 * @run main RMIConnectorServerTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;
import javax.management.remote.rmi.RMIConnector;
import javax.management.remote.rmi.RMIConnectorServer;

public class RMIConnectorServerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectorServer");
        check(JMXConnectorServer.class.isAssignableFrom(c), "extends JMXConnectorServer");
        Method[] mm = c.getMethods();
        check(has(mm, "start"), "start");
        check(has(mm, "stop"), "stop");
        check(has(mm, "isActive"), "isActive");
        check(has(mm, "getAddress"), "getAddress");
        check(has(mm, "getAttributes"), "getAttributes");
        check(has(mm, "toJMXConnector"), "toJMXConnector");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        check(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE.equals("jmx.remote.jndi.rebind"), "JNDI_REBIND_ATTRIBUTE");
        check(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE.equals("jmx.remote.rmi.client.socket.factory"), "RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE");
        check(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE.equals("jmx.remote.rmi.server.socket.factory"), "RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE");
        check(RMIConnectorServer.CREDENTIALS_FILTER_PATTERN.equals("jmx.remote.rmi.server.credentials.filter.pattern"), "CREDENTIALS_FILTER_PATTERN");
        check(RMIConnectorServer.SERIAL_FILTER_PATTERN.equals("jmx.remote.rmi.server.serial.filter.pattern"), "SERIAL_FILTER_PATTERN");

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        RMIConnectorServer server = new RMIConnectorServer(url, null, mbs);

        check(server.getMBeanServer() == mbs, "mbean server preserved");
        check(!server.isActive(), "not active");
        check(server.getAddress() == null, "inactive server address is null");
        check(server.getConnectionIds().length == 0, "no connections");
        Map<String, ?> attrs = server.getAttributes();
        check(attrs != null, "attributes non-null");
        check(attrs.isEmpty(), "empty attributes with null env");
    }

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

    static void checkThrowsMsg(Class<? extends Throwable> expected, String message,
                               ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && message.equals(t.getMessage())) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + "(" + message + ") but got " + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
    }

    // ---- deepened: constructor negatives ----
    static void testDeepCtorNegatives() throws Exception {
        checkThrowsMsg(IllegalArgumentException.class, "Null JMXServiceURL",
                new ThrowingRunnable() { public void run() throws IOException { new RMIConnectorServer(null, null); } },
                "null url ctor");
        checkThrowsMsg(MalformedURLException.class, "Invalid protocol type: bogus",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIConnectorServer(new JMXServiceURL("service:jmx:bogus://127.0.0.1"), null); } },
                "bogus protocol ctor");
        // Version difference vs JDK-8: iiop was removed, the JDK-27 constructor rejects it outright.
        checkThrowsMsg(MalformedURLException.class, "Invalid protocol type: iiop",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIConnectorServer(new JMXServiceURL("service:jmx:iiop://127.0.0.1"), null); } },
                "iiop protocol ctor rejected on JDK-27");
        checkThrowsMsg(MalformedURLException.class, "URL path must be empty or start with /jndi/",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIConnectorServer(new JMXServiceURL("service:jmx:rmi://127.0.0.1/foo"), null); } },
                "bad urlpath ctor");
    }

    // ---- deepened: lifecycle state machine ----
    static void testDeepLifecycle() throws Exception {
        final RMIConnectorServer unattached = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null);
        check(unattached.getAddress() == null, "inactive getAddress null");
        check(unattached.getConnectionIds().length == 0, "no connection ids");
        Map<String, ?> attrs = unattached.getAttributes();
        check(attrs.isEmpty(), "null env gives empty attributes");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = (Map<String, Object>) attrs;
            raw.put("k", "v");
            throw new AssertionError("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            passed++;
        }
        checkThrowsMsg(IllegalStateException.class, "This connector server is not attached to an MBean server",
                new ThrowingRunnable() { public void run() throws IOException { unattached.start(); } },
                "start without MBeanServer");
        checkThrowsMsg(IllegalStateException.class, "Connector is not active",
                new ThrowingRunnable() { public void run() throws IOException { unattached.toJMXConnector(null); } },
                "toJMXConnector while inactive");

        // stop() before start() permanently disables the server.
        final RMIConnectorServer stopped = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null,
                MBeanServerFactory.newMBeanServer());
        stopped.stop();
        check(!stopped.isActive(), "stop-before-start leaves server inactive");
        checkThrowsMsg(IOException.class, "The server has been stopped.",
                new ThrowingRunnable() { public void run() throws IOException { stopped.start(); } },
                "start after stop");
    }

    // ---- deepened: live loopback, MBean attribute routing, forwarder, notifications ----
    static void testDeepLive() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.csr27:type=Counter");
        mbs.registerMBean(new Counter(), on);

        // Recording MBeanServerForwarder built with a dynamic proxy.
        final MBeanServer[] holder = { mbs };
        final int[] forwardedCalls = { 0 };
        final MBeanServerForwarder fwd = (MBeanServerForwarder) Proxy.newProxyInstance(
                MBeanServerForwarder.class.getClassLoader(),
                new Class<?>[] { MBeanServerForwarder.class },
                new java.lang.reflect.InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] a) throws Throwable {
                        if (method.getName().equals("setMBeanServer")) { holder[0] = (MBeanServer) a[0]; return null; }
                        if (method.getName().equals("getMBeanServer")) return holder[0];
                        forwardedCalls[0]++;
                        try { return method.invoke(holder[0], a); }
                        catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
                    }
                });

        RMIConnectorServer cs = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, mbs);
        cs.setMBeanServerForwarder(fwd);
        check(cs.getMBeanServer() == fwd, "setMBeanServerForwarder re-points getMBeanServer");
        check(fwd.getMBeanServer() == mbs, "forwarder wraps original server");

        final List<Notification> notifs = Collections.synchronizedList(new ArrayList<Notification>());
        cs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { notifs.add(n); }
        }, null, null);

        cs.start();
        check(cs.isActive(), "active after start");
        cs.start(); // second start is a no-op
        check(cs.isActive(), "still active after repeat start");
        JMXServiceURL addr = cs.getAddress();
        check(addr != null, "address after start");
        check(addr.getProtocol().equals("rmi"), "encoded address keeps protocol rmi");
        check(addr.getHost().equals("127.0.0.1"), "encoded address host 127.0.0.1");
        check(addr.getURLPath().startsWith("/stub/"), "stub encoded in URL path: " + addr.getURLPath());

        JMXConnector c = JMXConnectorFactory.connect(addr, null);
        String cid = c.getConnectionId();
        check(cid.startsWith("rmi://127.0.0.1 "), "client id prefix: " + cid);
        String[] ids = cs.getConnectionIds();
        check(ids.length == 1 && ids[0].equals(cid), "connection id accounted: " + java.util.Arrays.toString(ids));
        MBeanServerConnection msc = c.getMBeanServerConnection();
        check(((Integer) msc.getAttribute(on, "Count")) == 0, "remote getAttribute through live server");
        check(forwardedCalls[0] > 0, "remote call routed through MBeanServerForwarder");
        c.close();
        check(cs.getConnectionIds().length == 0, "closed client removed from connection ids");

        // toJMXConnector on an active server returns a usable RMIConnector.
        RMIConnector viaStub = (RMIConnector) cs.toJMXConnector(null);
        check(viaStub != null, "toJMXConnector returns RMIConnector");
        viaStub.connect(null);
        check(((Integer) viaStub.getMBeanServerConnection().getAttribute(on, "Count")) == 0,
                "toJMXConnector client reads through");
        viaStub.close();

        // server-side OPENED/CLOSED notifications: c then viaStub each opened+closed = 4
        final long deadline = System.currentTimeMillis() + 5000;
        while (notifs.size() < 4 && System.currentTimeMillis() < deadline) Thread.sleep(50);
        check(notifs.size() == 4, "four server-side notifications, got " + notifs.size());
        boolean typesOk = notifs.size() == 4
                && notifs.get(0).getType().equals("jmx.remote.connection.opened")
                && notifs.get(1).getType().equals("jmx.remote.connection.closed")
                && notifs.get(2).getType().equals("jmx.remote.connection.opened")
                && notifs.get(3).getType().equals("jmx.remote.connection.closed");
        check(typesOk, "notification order opened/closed/opened/closed");
        boolean allConnNotif = true;
        for (Notification n : notifs)
            if (!(n instanceof JMXConnectionNotification)) allConnNotif = false;
        check(allConnNotif, "all are JMXConnectionNotification");
        check(notifs.get(0).getSource() == cs, "server notification source is the server");

        cs.stop();
        check(!cs.isActive(), "inactive after stop");
        check(cs.getAddress() == null, "address null after stop");
        cs.stop(); // stop is idempotent
        check(!cs.isActive(), "still inactive after repeat stop");
        checkThrowsMsg(IOException.class, "The server has been stopped.",
                new ThrowingRunnable() { public void run() throws IOException { cs.start(); } },
                "start after stop");

        // registered-as-MBean attribute routing: Active / Address / ConnectionIds
        MBeanServer reg = MBeanServerFactory.newMBeanServer();
        ObjectName csName = new ObjectName("jck.csr27:type=ConnectorServer");
        RMIConnectorServer mcs = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null);
        reg.registerMBean(mcs, csName);
        check(mcs.getMBeanServer() == reg, "preRegister attaches the registering server");
        check(((Boolean) reg.getAttribute(csName, "Active")).booleanValue() == false, "Active attribute false");
        reg.invoke(csName, "start", new Object[0], new String[0]);
        check(((Boolean) reg.getAttribute(csName, "Active")).booleanValue() == true, "Active attribute true");
        check(reg.getAttribute(csName, "Address").toString().equals(mcs.getAddress().toString()),
                "Address attribute equals live address");
        JMXConnector c2 = JMXConnectorFactory.connect(mcs.getAddress(), null);
        String[] mids = (String[]) reg.getAttribute(csName, "ConnectionIds");
        check(mids.length == 1 && mids[0].equals(c2.getConnectionId()),
                "ConnectionIds attribute reflects live client");
        c2.close();
        reg.invoke(csName, "stop", new Object[0], new String[0]);
        check(((Boolean) reg.getAttribute(csName, "Active")).booleanValue() == false, "Active false after stop");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCtorNegatives();
        testDeepLifecycle();
        testDeepLive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
