/*
 * @test
 * @summary javax.management.ReflectionException conformance: white-box structure + black-box behavior.
 * @run main ReflectionExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class ReflectionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.ReflectionException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.JMException".equals(c.getSuperclass().getName()), "superclass javax.management.JMException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor(Exception.class);
        check(ctor != null, "Exception constructor");
        ctor = c.getConstructor(Exception.class, String.class);
        check(ctor != null, "Exception + String constructor");
        c.getMethod("getTargetException");
        c.getMethod("getCause");
    }
    static void testBlackBox() throws Exception {
        Exception cause = new Exception("cause-message");
        ReflectionException e = new ReflectionException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("ReflectionException"), "toString contains class name");

        ReflectionException e2 = new ReflectionException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("ReflectionException"), "two-arg toString class name");

        Exception cause2 = new Exception("second");
        ReflectionException e3 = new ReflectionException(cause2);
        check(e3.getCause() == cause2, "independent instance target");
        check(e3.getMessage() == null, "independent instance message null");
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

    // ---- deepened (2026-11): null-target semantics / hierarchy / serialVersionUID / serialization ----

    static void testDeepNullTargetSemantics() {
        ReflectionException x = new ReflectionException(null);
        check(x.getTargetException() == null, "null target: getTargetException null");
        check(x.getCause() == null, "null target: getCause null");
        check(x.getMessage() == null, "null target: message null");
        ReflectionException y = new ReflectionException(null, "jck-msg");
        check("jck-msg".equals(y.getMessage()), "null target + message ctor: message preserved");
        check(y.getTargetException() == null, "null target + message ctor: target still null");
        check(y.getCause() == null, "null target + message ctor: cause null");
    }

    static void testDeepHierarchy() {
        check(ReflectionException.class.getSuperclass() == javax.management.JMException.class, "direct superclass JMException");
        check(new ReflectionException(new IllegalArgumentException("inner-msg")) instanceof java.io.Serializable, "Serializable");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ReflectionException.class).getSerialVersionUID() == 9170809325636915553L,
                "serialVersionUID constant");
        ReflectionException src = new ReflectionException(new IllegalArgumentException("inner-msg"), "outer-msg");
        ReflectionException back = deepRoundTrip(src);
        check("outer-msg".equals(back.getMessage()), "round-trip: message preserved");
        check(back.getTargetException() instanceof IllegalArgumentException, "round-trip: target type");
        check("inner-msg".equals(back.getTargetException().getMessage()), "round-trip: target message preserved");
        check(back.getCause() == back.getTargetException(), "round-trip: getCause() == getTargetException()");
    }

    public static interface ProbeMBean { int getN(); }
    public static class Probe implements ProbeMBean { public int getN() { return 1; } }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.reflectionexception:type=Probe");
        server.registerMBean(new Probe(), name);
        try {
            server.invoke(name, "noSuchOp", new Object[0], new String[0]);
            check(false, "invoking a missing operation must throw");
        } catch (ReflectionException e) {
            check(e.getTargetException() instanceof NoSuchMethodException, "live trigger: wraps NoSuchMethodException");
            check(e.getTargetException() == e.getCause(), "live trigger: target == cause");
        }
        try {
            server.getAttribute(name, "nope");
            check(false, "unreachable: getAttribute on missing attribute");
        } catch (AttributeNotFoundException e) {
            passed++; // distinct path: reflection errors are ReflectionException, missing attrs are not
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
