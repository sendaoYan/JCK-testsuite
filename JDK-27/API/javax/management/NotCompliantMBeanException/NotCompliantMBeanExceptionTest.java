/*
 * @test
 * @summary javax.management.NotCompliantMBeanException conformance: white-box structure + black-box behavior.
 * @run main NotCompliantMBeanExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class NotCompliantMBeanExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.NotCompliantMBeanException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.OperationsException".equals(c.getSuperclass().getName()), "superclass javax.management.OperationsException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");

        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor();
        check(ctor != null, "no-arg constructor");
        ctor = c.getConstructor(String.class);
        check(ctor != null, "String constructor");
    }
    static void testBlackBox() throws Exception {
        Exception e = new NotCompliantMBeanException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("NotCompliantMBeanException"), "toString contains class name");
        check(e instanceof javax.management.OperationsException, "instanceof OperationsException");
        check(e instanceof javax.management.JMException, "instanceof JMException");
        Exception e2 = new NotCompliantMBeanException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("NotCompliantMBeanException"), "no-arg toString class name");
        Exception e3 = new NotCompliantMBeanException((String) null);
        check(e3.getMessage() == null, "null String message null");

    }


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
        NotCompliantMBeanException noArg = new NotCompliantMBeanException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        NotCompliantMBeanException msg = new NotCompliantMBeanException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        NotCompliantMBeanException nullMsg = new NotCompliantMBeanException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(NotCompliantMBeanException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(NotCompliantMBeanException.class).getSerialVersionUID() == 5175579583207963577L,
                "serialVersionUID constant (" + 5175579583207963577L + ")");
        NotCompliantMBeanException backNo = deepRoundTrip(new NotCompliantMBeanException());
        check(backNo instanceof NotCompliantMBeanException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        NotCompliantMBeanException backMsg = deepRoundTrip(new NotCompliantMBeanException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("NotCompliantMBeanException");
    }

    public static class Plain { } // no XxxMBean interface, no DynamicMBean: not JMX-compliant

    // ---- deepened (2026-11): live MBeanServer trigger (compliance check) ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.notcompliant:type=Plain");
        try {
            server.registerMBean(new Plain(), name);
            check(false, "registering a non-compliant class must throw");
        } catch (NotCompliantMBeanException e) {
            check(e.getMessage() != null && e.getMessage().contains("does not implement DynamicMBean"),
                    "live trigger message mentions compliance conventions");
        }
        check(!server.isRegistered(name), "state invariant: nothing registered after rejection");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
