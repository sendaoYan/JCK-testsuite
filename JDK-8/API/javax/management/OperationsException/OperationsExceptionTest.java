/*
 * @test
 * @summary javax.management.OperationsException conformance: exception hierarchy and construction.
 * @run main OperationsExceptionTest
 */
import javax.management.*;
import java.io.*;

public class OperationsExceptionTest {
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

    // ---- deepened (2026-11): ctor chain / serialVersionUID / serialization / subclass table ----

    static void testDeepCtorChain() {
        OperationsException noArg = new OperationsException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        OperationsException msg = new OperationsException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        OperationsException nullMsg = new OperationsException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(OperationsException.class.getSuperclass() == javax.management.JMException.class, "direct superclass JMException");
        check(new OperationsException("m") instanceof javax.management.JMException, "extends JMException");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(OperationsException.class).getSerialVersionUID() == -4967597595580536216L,
                "serialVersionUID constant");
        OperationsException backNo = deepRoundTrip(new OperationsException());
        check(backNo instanceof OperationsException && backNo.getMessage() == null, "round-trip no-arg");
        OperationsException backMsg = deepRoundTrip(new OperationsException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
    }

    static void testDeepSubclassTable() {
        Class<?>[] subs = {
            javax.management.AttributeNotFoundException.class,
            javax.management.InstanceAlreadyExistsException.class,
            javax.management.InstanceNotFoundException.class,
            javax.management.IntrospectionException.class,
            javax.management.InvalidAttributeValueException.class,
            javax.management.ListenerNotFoundException.class,
            javax.management.MalformedObjectNameException.class,
            javax.management.NotCompliantMBeanException.class,
            javax.management.ServiceNotFoundException.class,
        };
        for (Class<?> c : subs) {
            check(OperationsException.class.isAssignableFrom(c), c.getSimpleName() + " is a OperationsException");
        }
    }

    public static void main(String[] args) throws Exception {
        javax.management.OperationsException x = new javax.management.OperationsException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.JMException").isAssignableFrom(c), "extends javax.management.JMException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepSubclassTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
