/*
 * @test
 * @summary javax.management.JMRuntimeException conformance: exception hierarchy and construction.
 * @run main JMRuntimeExceptionTest
 */
import javax.management.*;
import java.io.*;

public class JMRuntimeExceptionTest {
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
        JMRuntimeException noArg = new JMRuntimeException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        JMRuntimeException msg = new JMRuntimeException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        JMRuntimeException nullMsg = new JMRuntimeException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(JMRuntimeException.class.getSuperclass() == RuntimeException.class, "direct superclass RuntimeException");
        check(new JMRuntimeException() instanceof RuntimeException, "runtime root");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(JMRuntimeException.class).getSerialVersionUID() == 6573344628407841861L,
                "serialVersionUID constant");
        JMRuntimeException backNo = deepRoundTrip(new JMRuntimeException());
        check(backNo instanceof JMRuntimeException && backNo.getMessage() == null, "round-trip no-arg");
        JMRuntimeException backMsg = deepRoundTrip(new JMRuntimeException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
    }

    static void testDeepSubclassTable() {
        Class<?>[] subs = {
            javax.management.RuntimeErrorException.class,
            javax.management.RuntimeMBeanException.class,
            javax.management.RuntimeOperationsException.class,
            // (MonitorSettingException / RelationServiceNotRegisteredException / openmbean
            //  runtime exceptions are exercised in their own package tests)
        };
        for (Class<?> c : subs) {
            check(JMRuntimeException.class.isAssignableFrom(c), c.getSimpleName() + " is a JMRuntimeException");
        }
    }

    public static void main(String[] args) throws Exception {
        javax.management.JMRuntimeException x = new javax.management.JMRuntimeException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.RuntimeException").isAssignableFrom(c), "extends java.lang.RuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepSubclassTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
