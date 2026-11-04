/*
 * @test
 * @summary com.sun.nio.sctp.SendFailedNotification conformance: hierarchy and declared members via reflection.
 * @run main SendFailedNotificationTest
 */

public class SendFailedNotificationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.sctp.SendFailedNotification", false, null);
        check(c.getName().equals("com.sun.nio.sctp.SendFailedNotification"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.nio.sctp.Notification"), "implements com.sun.nio.sctp.Notification");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("address"), "declares address");
        check(ms.contains("association"), "declares association");
        check(ms.contains("buffer"), "declares buffer");
        check(ms.contains("errorCode"), "declares errorCode");
        check(ms.contains("streamNumber"), "declares streamNumber");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
