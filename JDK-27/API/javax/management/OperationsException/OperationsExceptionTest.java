/*
 * @test
 * @summary javax.management.OperationsException conformance: white-box structure + black-box behavior.
 * @run main OperationsExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class OperationsExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.OperationsException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.JMException".equals(c.getSuperclass().getName()), "superclass javax.management.JMException");
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
        Exception e = new OperationsException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("OperationsException"), "toString contains class name");
        check(e instanceof javax.management.JMException, "instanceof JMException");
        check(e instanceof Exception, "instanceof Exception");
        Exception e2 = new OperationsException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("OperationsException"), "no-arg toString class name");
        Exception e3 = new OperationsException((String) null);
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
        testWhiteBox();
        testBlackBox();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepSubclassTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
