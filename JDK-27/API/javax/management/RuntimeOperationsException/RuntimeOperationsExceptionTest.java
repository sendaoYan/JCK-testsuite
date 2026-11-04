/*
 * @test
 * @summary javax.management.RuntimeOperationsException conformance: white-box structure + black-box behavior.
 * @run main RuntimeOperationsExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class RuntimeOperationsExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.RuntimeOperationsException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.JMRuntimeException".equals(c.getSuperclass().getName()), "superclass javax.management.JMRuntimeException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor(RuntimeException.class);
        check(ctor != null, "RuntimeException constructor");
        ctor = c.getConstructor(RuntimeException.class, String.class);
        check(ctor != null, "RuntimeException + String constructor");
        c.getMethod("getTargetException");
        c.getMethod("getCause");
    }
    static void testBlackBox() throws Exception {
        RuntimeException cause = new RuntimeException("cause-message");
        RuntimeOperationsException e = new RuntimeOperationsException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("RuntimeOperationsException"), "toString contains class name");

        RuntimeOperationsException e2 = new RuntimeOperationsException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("RuntimeOperationsException"), "two-arg toString class name");

        RuntimeException cause2 = new RuntimeException("second");
        RuntimeOperationsException e3 = new RuntimeOperationsException(cause2);
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
        RuntimeOperationsException x = new RuntimeOperationsException(null);
        check(x.getTargetException() == null, "null target: getTargetException null");
        check(x.getCause() == null, "null target: getCause null");
        check(x.getMessage() == null, "null target: message null");
        RuntimeOperationsException y = new RuntimeOperationsException(null, "jck-msg");
        check("jck-msg".equals(y.getMessage()), "null target + message ctor: message preserved");
        check(y.getTargetException() == null, "null target + message ctor: target still null");
        check(y.getCause() == null, "null target + message ctor: cause null");
    }

    static void testDeepHierarchy() {
        check(RuntimeOperationsException.class.getSuperclass() == javax.management.JMRuntimeException.class, "direct superclass JMRuntimeException");
        check(new RuntimeOperationsException(new IllegalStateException("inner-msg")) instanceof java.io.Serializable, "Serializable");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(RuntimeOperationsException.class).getSerialVersionUID() == -8408923047489133588L,
                "serialVersionUID constant");
        RuntimeOperationsException src = new RuntimeOperationsException(new IllegalStateException("inner-msg"), "outer-msg");
        RuntimeOperationsException back = deepRoundTrip(src);
        check("outer-msg".equals(back.getMessage()), "round-trip: message preserved");
        check(back.getTargetException() instanceof IllegalStateException, "round-trip: target type");
        check("inner-msg".equals(back.getTargetException().getMessage()), "round-trip: target message preserved");
        check(back.getCause() == back.getTargetException(), "round-trip: getCause() == getTargetException()");
    }

    // ---- deepened (2026-11): live trigger via Attribute contract ----

    static void testDeepLiveTrigger() throws Exception {
        try {
            new Attribute(null, "v");
            check(false, "Attribute(null, v) must throw");
        } catch (RuntimeOperationsException e) {
            check(e.getTargetException() instanceof IllegalArgumentException, "wraps IllegalArgumentException");
            check(e.getMessage() == null, "outer message stays null (inner not propagated)");
        }
        // typed AttributeList overloads wrap index errors
        try {
            new AttributeList().add(5, new Attribute("A", Integer.valueOf(1)));
            check(false, "out-of-range typed add must throw");
        } catch (RuntimeOperationsException e) {
            check(e.getTargetException() instanceof IndexOutOfBoundsException, "wraps IndexOutOfBoundsException");
        }
    }


    static void testDeepVersionSpecificTrigger() throws Exception {
        // JDK-27: registerMBean(null, name) is rejected with RuntimeOperationsException
        // (JDK-8 throws a plain NullPointerException - locked separately on that side)
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.runtimeops27:type=Null");
        try {
            server.registerMBean(null, name);
            check(false, "registerMBean(null, ...) must throw");
        } catch (RuntimeOperationsException e) {
            check(e.getTargetException() instanceof IllegalArgumentException, "wraps IllegalArgumentException");
        }
        check(!server.isRegistered(name), "nothing registered after null rejection");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        testDeepVersionSpecificTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
