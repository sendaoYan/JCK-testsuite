/*
 * @test
 * @summary javax.management.RuntimeMBeanException conformance: white-box structure + black-box behavior.
 * @run main RuntimeMBeanExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class RuntimeMBeanExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.RuntimeMBeanException");
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
        RuntimeMBeanException e = new RuntimeMBeanException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("RuntimeMBeanException"), "toString contains class name");

        RuntimeMBeanException e2 = new RuntimeMBeanException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("RuntimeMBeanException"), "two-arg toString class name");

        RuntimeException cause2 = new RuntimeException("second");
        RuntimeMBeanException e3 = new RuntimeMBeanException(cause2);
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
        RuntimeMBeanException x = new RuntimeMBeanException(null);
        check(x.getTargetException() == null, "null target: getTargetException null");
        check(x.getCause() == null, "null target: getCause null");
        check(x.getMessage() == null, "null target: message null");
        RuntimeMBeanException y = new RuntimeMBeanException(null, "jck-msg");
        check("jck-msg".equals(y.getMessage()), "null target + message ctor: message preserved");
        check(y.getTargetException() == null, "null target + message ctor: target still null");
        check(y.getCause() == null, "null target + message ctor: cause null");
    }

    static void testDeepHierarchy() {
        check(RuntimeMBeanException.class.getSuperclass() == javax.management.JMRuntimeException.class, "direct superclass JMRuntimeException");
        check(new RuntimeMBeanException(new RuntimeException()).getTargetException() instanceof RuntimeException, "getTargetException is a RuntimeException");
        check(new RuntimeMBeanException(new IllegalStateException("inner-msg")) instanceof java.io.Serializable, "Serializable");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(RuntimeMBeanException.class).getSerialVersionUID() == 5274912751982730171L,
                "serialVersionUID constant");
        RuntimeMBeanException src = new RuntimeMBeanException(new IllegalStateException("inner-msg"), "outer-msg");
        RuntimeMBeanException back = deepRoundTrip(src);
        check("outer-msg".equals(back.getMessage()), "round-trip: message preserved");
        check(back.getTargetException() instanceof IllegalStateException, "round-trip: target type");
        check("inner-msg".equals(back.getTargetException().getMessage()), "round-trip: target message preserved");
        check(back.getCause() == back.getTargetException(), "round-trip: getCause() == getTargetException()");
    }

    public static interface ProbeMBean { void boomRte(); }
    public static class Probe implements ProbeMBean {
        public void boomRte() { throw new IllegalStateException("user-rte"); }
    }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.runtimembeanexception:type=Probe");
        server.registerMBean(new Probe(), name);
        try {
            server.invoke(name, "boomRte", new Object[0], new String[0]);
            check(false, "MBean method throwing RuntimeException must surface as RuntimeMBeanException");
        } catch (RuntimeMBeanException e) {
            check(e.getTargetException() instanceof IllegalStateException, "live trigger: wraps user RTE");
            check("user-rte".equals(e.getTargetException().getMessage()), "live trigger: user message");
            check(e.getCause() == e.getTargetException(), "live trigger: cause == target");
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
