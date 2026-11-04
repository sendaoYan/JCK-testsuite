/*
 * @test
 * @summary com.sun.jmx.remote.internal.ServerNotifForwarder conformance: hierarchy and declared members via reflection.
 * @run main ServerNotifForwarderTest
 */

public class ServerNotifForwarderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.internal.ServerNotifForwarder", false, null);
        check(c.getName().equals("com.sun.jmx.remote.internal.ServerNotifForwarder"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addNotificationListener"), "declares addNotificationListener");
        check(ms.contains("allowNotificationEmission"), "declares allowNotificationEmission");
        check(ms.contains("checkMBeanPermission"), "declares checkMBeanPermission");
        check(ms.contains("checkState"), "declares checkState");
        check(ms.contains("extractException"), "declares extractException");
        check(ms.contains("fetchNotifs"), "declares fetchNotifs");
        check(ms.contains("getListenerID"), "declares getListenerID");
        check(ms.contains("getSubject"), "declares getSubject");
        check(ms.contains("removeNotificationListener"), "declares removeNotificationListener");
        check(ms.contains("snoopOnUnregister"), "declares snoopOnUnregister");
        check(ms.contains("terminate"), "declares terminate");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
