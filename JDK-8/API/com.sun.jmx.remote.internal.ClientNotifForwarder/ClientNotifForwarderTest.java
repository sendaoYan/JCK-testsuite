/*
 * @test
 * @summary com.sun.jmx.remote.internal.ClientNotifForwarder conformance: hierarchy and declared members via reflection.
 * @run main ClientNotifForwarderTest
 */

public class ClientNotifForwarderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.internal.ClientNotifForwarder", false, null);
        check(c.getName().equals("com.sun.jmx.remote.internal.ClientNotifForwarder"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addListenerForMBeanRemovedNotif"), "declares addListenerForMBeanRemovedNotif");
        check(ms.contains("addNotificationListener"), "declares addNotificationListener");
        check(ms.contains("beforeRemove"), "declares beforeRemove");
        check(ms.contains("fetchNotifs"), "declares fetchNotifs");
        check(ms.contains("init"), "declares init");
        check(ms.contains("lostNotifs"), "declares lostNotifs");
        check(ms.contains("postReconnection"), "declares postReconnection");
        check(ms.contains("preReconnection"), "declares preReconnection");
        check(ms.contains("removeListenerForMBeanRemovedNotif"), "declares removeListenerForMBeanRemovedNotif");
        check(ms.contains("removeNotificationListener"), "declares removeNotificationListener");
        check(ms.contains("setState"), "declares setState");
        check(ms.contains("terminate"), "declares terminate");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
