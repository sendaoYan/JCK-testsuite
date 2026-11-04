/*
 * @test
 * @summary javax.management.JMRuntimeException conformance: white-box structure + black-box behavior.
 * @run main JMRuntimeExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class JMRuntimeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.JMRuntimeException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.RuntimeException".equals(c.getSuperclass().getName()), "superclass java.lang.RuntimeException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(RuntimeException.class.isAssignableFrom(c), "runtime exception");

        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor();
        check(ctor != null, "no-arg constructor");
        ctor = c.getConstructor(String.class);
        check(ctor != null, "String constructor");
    }
    static void testBlackBox() throws Exception {
        Exception e = new JMRuntimeException("fixed-message");
        check("fixed-message".equals(e.getMessage()), "message preserved");
        check(e.toString().contains("JMRuntimeException"), "toString contains class name");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        Exception e2 = new JMRuntimeException();
        check(e2.getMessage() == null, "no-arg message null");
        check(e2.toString().contains("JMRuntimeException"), "no-arg toString class name");
        Exception e3 = new JMRuntimeException((String) null);
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
        testWhiteBox();
        testBlackBox();
        testDeepCtorChain();
        testDeepSerialization();
        testDeepSubclassTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
