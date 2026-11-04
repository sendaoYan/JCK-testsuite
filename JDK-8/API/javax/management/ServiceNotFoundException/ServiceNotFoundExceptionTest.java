/*
 * @test
 * @summary javax.management.ServiceNotFoundException conformance: exception hierarchy and construction.
 * @run main ServiceNotFoundExceptionTest
 */
import javax.management.*;
import java.io.*;

public class ServiceNotFoundExceptionTest {
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
        ServiceNotFoundException noArg = new ServiceNotFoundException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        ServiceNotFoundException msg = new ServiceNotFoundException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        ServiceNotFoundException nullMsg = new ServiceNotFoundException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(ServiceNotFoundException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ServiceNotFoundException.class).getSerialVersionUID() == -3990675661956646827L,
                "serialVersionUID constant (" + -3990675661956646827L + ")");
        ServiceNotFoundException backNo = deepRoundTrip(new ServiceNotFoundException());
        check(backNo instanceof ServiceNotFoundException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        ServiceNotFoundException backMsg = deepRoundTrip(new ServiceNotFoundException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("ServiceNotFoundException");
    }

    // ---- deepened (2026-11): live trigger via MLet (JDK-8 only: MLet removed in JDK-27) ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        javax.management.loading.MLet mlet = new javax.management.loading.MLet();
        server.registerMBean(mlet, new ObjectName("jck.servicenotfound:type=MLet"));
        java.net.URL missing = new java.io.File(System.getProperty("java.io.tmpdir"),
                "jck-no-such-mlet-file.mlet").toURI().toURL();
        try {
            mlet.getMBeansFromURL(missing);
            check(false, "getMBeansFromURL on a missing file must throw");
        } catch (ServiceNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("jck-no-such-mlet-file.mlet"),
                    "live trigger message mentions the URL");
        }
    }

    public static void main(String[] args) throws Exception {
        javax.management.ServiceNotFoundException x = new javax.management.ServiceNotFoundException("msg");
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
