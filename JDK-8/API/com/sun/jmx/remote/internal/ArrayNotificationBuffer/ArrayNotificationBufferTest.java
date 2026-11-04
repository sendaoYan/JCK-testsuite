/*
 * @test
 * @summary com.sun.jmx.remote.internal.ArrayNotificationBuffer conformance: hierarchy and declared members via reflection.
 * @run main ArrayNotificationBufferTest
 */

public class ArrayNotificationBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.internal.ArrayNotificationBuffer", false, null);
        check(c.getName().equals("com.sun.jmx.remote.internal.ArrayNotificationBuffer"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jmx.remote.internal.NotificationBuffer"), "implements com.sun.jmx.remote.internal.NotificationBuffer");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addBufferListener"), "declares addBufferListener");
        check(ms.contains("addNotification"), "declares addNotification");
        check(ms.contains("addNotificationListener"), "declares addNotificationListener");
        check(ms.contains("addSharer"), "declares addSharer");
        check(ms.contains("checkNoLocks"), "declares checkNoLocks");
        check(ms.contains("createListeners"), "declares createListeners");
        check(ms.contains("createdNotification"), "declares createdNotification");
        check(ms.contains("destroyListeners"), "declares destroyListeners");
        check(ms.contains("dispose"), "declares dispose");
        check(ms.contains("dropNotification"), "declares dropNotification");
        check(ms.contains("earliestSequenceNumber"), "declares earliestSequenceNumber");
        check(ms.contains("extractException"), "declares extractException");
        check(ms.contains("fetchNotifications"), "declares fetchNotifications");
        check(ms.contains("getNotificationBuffer"), "declares getNotificationBuffer");
        check(ms.contains("isDisposed"), "declares isDisposed");
        check(ms.contains("isInstanceOf"), "declares isInstanceOf");
        check(ms.contains("nextSequenceNumber"), "declares nextSequenceNumber");
        check(ms.contains("notificationAt"), "declares notificationAt");
        check(ms.contains("queryNames"), "declares queryNames");
        check(ms.contains("removeBufferListener"), "declares removeBufferListener");
        check(ms.contains("removeNotificationBuffer"), "declares removeNotificationBuffer");
        check(ms.contains("removeNotificationListener"), "declares removeNotificationListener");
        check(ms.contains("removeSharer"), "declares removeSharer");
        check(ms.contains("resize"), "declares resize");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
