/*
 * @test
 * @summary javax.management.ListenerNotFoundException conformance: exception hierarchy and construction.
 * @run main ListenerNotFoundExceptionTest
 */
import javax.management.*;
import java.io.*;

public class ListenerNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T deepRoundTrip(T t) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ---- deepened (2026-11): ctor chain / hierarchy / serialVersionUID / serialization ----

    static void testDeepCtorChain() {
        ListenerNotFoundException noArg = new ListenerNotFoundException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        ListenerNotFoundException msg = new ListenerNotFoundException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        ListenerNotFoundException nullMsg = new ListenerNotFoundException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(ListenerNotFoundException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ListenerNotFoundException.class).getSerialVersionUID() == -7242605822448519061L,
                "serialVersionUID constant (" + -7242605822448519061L + ")");
        ListenerNotFoundException backNo = deepRoundTrip(new ListenerNotFoundException());
        check(backNo instanceof ListenerNotFoundException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        ListenerNotFoundException backMsg = deepRoundTrip(new ListenerNotFoundException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("ListenerNotFoundException");
    }

    // ---- deepened (2026-11): live MBeanServer trigger (server delegate broadcaster) ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName delegate = new ObjectName("JMImplementation:type=MBeanServerDelegate");
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { }
        };
        server.addNotificationListener(delegate, listener, null, null);
        server.removeNotificationListener(delegate, listener);
        try {
            server.removeNotificationListener(delegate, listener);
            check(false, "removing an already-removed listener must throw");
        } catch (ListenerNotFoundException e) {
            passed++;
        }
        try {
            server.removeNotificationListener(delegate, new NotificationListener() {
                public void handleNotification(Notification n, Object h) { }
            });
            check(false, "removing a never-added listener must throw");
        } catch (ListenerNotFoundException e) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        javax.management.ListenerNotFoundException x = new javax.management.ListenerNotFoundException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.OperationsException").isAssignableFrom(c), "extends javax.management.OperationsException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
