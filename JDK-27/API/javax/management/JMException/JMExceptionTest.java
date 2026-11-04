/*
 * @test
 * @summary javax.management.JMException conformance: white-box structure + black-box behavior.
 * @run main JMExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class JMExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.JMException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "superclass java.lang.Exception");
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
        Exception e = new JMException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("JMException"), "toString contains class name");
        check(e instanceof Exception, "instanceof Exception");
        check(!(e instanceof RuntimeException), "checked, not runtime");
        Exception e2 = new JMException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("JMException"), "no-arg toString class name");
        Exception e3 = new JMException((String) null);
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
        testWhiteBox();
        testBlackBox();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepSubclassTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
