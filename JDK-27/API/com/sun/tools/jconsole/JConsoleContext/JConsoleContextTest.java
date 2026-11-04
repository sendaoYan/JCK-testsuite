/*
 * @test
 * @summary com.sun.tools.jconsole.JConsoleContext conformance: white-box structure + black-box behavior.
 * @run main JConsoleContextTest
 */

import java.lang.reflect.*;
import java.util.*;

public class JConsoleContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.tools.jconsole.JConsoleContext");
        check(c.isInterface(), "interface");
        Set<String> m = methodNames(c);
        check(m.contains("getMBeanServerConnection"), "getMBeanServerConnection");
        check(m.contains("getConnectionState"), "getConnectionState");
        check(m.contains("addPropertyChangeListener"), "addPropertyChangeListener");
        check(m.contains("removePropertyChangeListener"), "removePropertyChangeListener");
        check(fieldNames(c).contains("CONNECTION_STATE_PROPERTY"), "CONNECTION_STATE_PROPERTY");
    }

    static void testBlackBox() throws Exception {
        check(com.sun.tools.jconsole.JConsoleContext.CONNECTION_STATE_PROPERTY.equals("connectionState"), "CONNECTION_STATE_PROPERTY");
        com.sun.tools.jconsole.JConsoleContext ctx = new com.sun.tools.jconsole.JConsoleContext() {
            public javax.management.MBeanServerConnection getMBeanServerConnection() { return null; }
            public com.sun.tools.jconsole.JConsoleContext.ConnectionState getConnectionState() { return com.sun.tools.jconsole.JConsoleContext.ConnectionState.DISCONNECTED; }
            public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}
            public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {}
        };
        check(ctx.getConnectionState() == com.sun.tools.jconsole.JConsoleContext.ConnectionState.DISCONNECTED, "getConnectionState");
        check(ctx.getMBeanServerConnection() == null, "getMBeanServerConnection");
        ctx.addPropertyChangeListener(null);
        ctx.removePropertyChangeListener(null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
