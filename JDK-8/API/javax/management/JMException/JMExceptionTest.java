/*
 * @test
 * @summary javax.management.JMException conformance: exception hierarchy and construction.
 * @run main JMExceptionTest
 */
import javax.management.*;
import java.io.*;

public class JMExceptionTest {
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
        JMException noArg = new JMException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        JMException msg = new JMException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        JMException nullMsg = new JMException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(JMException.class.getSuperclass() == Exception.class, "direct superclass Exception");
        check(new JMException() instanceof Exception
                && !RuntimeException.class.isAssignableFrom(JMException.class),
                "checked root of the management exception hierarchy");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(JMException.class).getSerialVersionUID() == 350520924977331825L,
                "serialVersionUID constant");
        JMException backNo = deepRoundTrip(new JMException());
        check(backNo instanceof JMException && backNo.getMessage() == null, "round-trip no-arg");
        JMException backMsg = deepRoundTrip(new JMException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
    }

    static void testDeepSubclassTable() {
        Class<?>[] subs = {
            javax.management.ReflectionException.class,
            javax.management.MBeanException.class,
            javax.management.MBeanRegistrationException.class,
            javax.management.OperationsException.class,
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
            check(JMException.class.isAssignableFrom(c), c.getSimpleName() + " is a JMException");
        }
    }

    public static void main(String[] args) throws Exception {
        javax.management.JMException x = new javax.management.JMException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.Exception").isAssignableFrom(c), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepSubclassTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
