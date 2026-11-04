/*
 * @test
 * @summary javax.management.InstanceNotFoundException conformance: white-box structure + black-box behavior.
 * @run main InstanceNotFoundExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class InstanceNotFoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.InstanceNotFoundException");
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
        Exception e = new InstanceNotFoundException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("InstanceNotFoundException"), "toString contains class name");
        check(e instanceof javax.management.OperationsException, "instanceof OperationsException");
        check(e instanceof javax.management.JMException, "instanceof JMException");
        Exception e2 = new InstanceNotFoundException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("InstanceNotFoundException"), "no-arg toString class name");
        Exception e3 = new InstanceNotFoundException((String) null);
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
        InstanceNotFoundException noArg = new InstanceNotFoundException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        InstanceNotFoundException msg = new InstanceNotFoundException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        InstanceNotFoundException nullMsg = new InstanceNotFoundException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(InstanceNotFoundException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(InstanceNotFoundException.class).getSerialVersionUID() == -882579438394773049L,
                "serialVersionUID constant (" + -882579438394773049L + ")");
        InstanceNotFoundException backNo = deepRoundTrip(new InstanceNotFoundException());
        check(backNo instanceof InstanceNotFoundException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        InstanceNotFoundException backMsg = deepRoundTrip(new InstanceNotFoundException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("InstanceNotFoundException");
    }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName missing = new ObjectName("jck.instancenotfound:type=Ghost");
        check(!server.isRegistered(missing), "ghost name not registered");
        try {
            server.getObjectInstance(missing);
            check(false, "getObjectInstance on unregistered name must throw");
        } catch (InstanceNotFoundException e) {
            passed++;
        }
        try {
            server.getAttribute(missing, "Count");
            check(false, "getAttribute on unregistered name must throw");
        } catch (InstanceNotFoundException e) {
            passed++;
        }
        try {
            server.unregisterMBean(missing);
            check(false, "unregisterMBean on unregistered name must throw");
        } catch (InstanceNotFoundException e) {
            passed++;
        }
        check(!server.isRegistered(missing), "state invariant: still unregistered after failures");
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
