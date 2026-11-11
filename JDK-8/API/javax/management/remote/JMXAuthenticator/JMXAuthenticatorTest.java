/*
 * @test
 * @summary javax.management.remote.JMXAuthenticator conformance: interface reflection.
 * @run main JMXAuthenticatorTest
 */
import javax.management.*;
import javax.management.remote.*;


public class JMXAuthenticatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXAuthenticator");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "authenticate"), "declares authenticate");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): interface contract + real authentication over RMI loopback ====
    static void testDeepened() throws Exception {
        // Interface shape: authenticate(Object) returning Subject; no checked exceptions declared
        // (rejections travel as the unchecked SecurityException, per the specification).
        java.lang.reflect.Method m = JMXAuthenticator.class.getMethod("authenticate", Object.class);
        check(m.getReturnType() == javax.security.auth.Subject.class, "d-authenticate returns Subject");
        check(m.getExceptionTypes().length == 0, "d-authenticate declares no checked exceptions");

        // User implementation: credential inspection, Subject minting, rejection path.
        final java.util.List<Object> received = java.util.Collections.synchronizedList(new java.util.ArrayList<Object>());
        final javax.security.auth.Subject granted = new javax.security.auth.Subject();
        granted.getPrincipals().add(new JMXPrincipal("alice"));
        JMXAuthenticator auth = new JMXAuthenticator() {
            public javax.security.auth.Subject authenticate(Object credentials) {
                received.add(credentials);
                if (credentials == null
                        || (credentials instanceof String[] && "alice".equals(((String[]) credentials)[0]))) {
                    return granted;
                }
                throw new SecurityException("bad credentials");
            }
        };
        check(auth.authenticate(new String[]{ "alice", "pw" }) == granted, "d-good credentials grant subject");
        check(auth.authenticate(null) == granted, "d-null credentials reach the authenticator");
        try {
            auth.authenticate(new String[]{ "mallory", "pw" });
            check(false, "d-bad credentials should throw");
        } catch (SecurityException e) {
            check("bad credentials".equals(e.getMessage()), "d-rejection message: " + e.getMessage());
        }

        // Real closed loop: wire the authenticator into a connector server via AUTHENTICATOR.
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.auth:type=Counter");
        server.registerMBean(new RemCounter(), on);
        java.util.Map<String, Object> env = new java.util.HashMap<String, Object>();
        env.put(JMXConnectorServer.AUTHENTICATOR, new JMXAuthenticator() {
            public javax.security.auth.Subject authenticate(Object credentials) {
                if (credentials instanceof String[] && "alice".equals(((String[]) credentials)[0])) {
                    javax.security.auth.Subject s = new javax.security.auth.Subject();
                    s.getPrincipals().add(new JMXPrincipal("alice"));
                    return s;
                }
                throw new SecurityException("bad credentials");
            }
        });
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), env, server);
        cs.start();
        try {
            // Valid credentials: connection succeeds and the authenticated principal appears in the id.
            java.util.Map<String, Object> good = new java.util.HashMap<String, Object>();
            good.put(JMXConnector.CREDENTIALS, new String[]{ "alice", "pw" });
            JMXConnector conn = JMXConnectorFactory.connect(cs.getAddress(), good);
            try {
                String id = conn.getConnectionId();
                check(id.startsWith("rmi://") && id.contains("alice"), "d-authenticated id carries principal: " + id);
                check(((Integer) conn.getMBeanServerConnection().getAttribute(on, "Count")).intValue() == 0,
                        "d-authenticated remote read");
            } finally {
                conn.close();
            }
            // Invalid credentials: the authenticator's SecurityException reaches the caller as-is.
            java.util.Map<String, Object> bad = new java.util.HashMap<String, Object>();
            bad.put(JMXConnector.CREDENTIALS, new String[]{ "mallory", "pw" });
            try {
                JMXConnectorFactory.connect(cs.getAddress(), bad);
                check(false, "d-bad credentials connect should throw");
            } catch (SecurityException e) {
                check("bad credentials".equals(e.getMessage()), "d-bad credentials message propagated: " + e.getMessage());
            }
        } finally {
            cs.stop();
        }
    }

    // ---- deepened fixture ----
    public static interface RemCounterMBean {
        int getCount();
    }
    public static class RemCounter implements RemCounterMBean {
        private int count;
        public int getCount() { return count; }
    }

}
