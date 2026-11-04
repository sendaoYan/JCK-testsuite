/*
 * @test
 * @summary javax.management.MBeanException conformance: white-box structure + black-box behavior.
 * @run main MBeanExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class MBeanExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanException");
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
        MBeanException e = new MBeanException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("MBeanException"), "toString contains class name");

        MBeanException e2 = new MBeanException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("MBeanException"), "two-arg toString class name");

        Exception cause2 = new Exception("second");
        MBeanException e3 = new MBeanException(cause2);
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
        MBeanException x = new MBeanException(null);
        check(x.getTargetException() == null, "null target: getTargetException null");
        check(x.getCause() == null, "null target: getCause null");
        check(x.getMessage() == null, "null target: message null");
        MBeanException y = new MBeanException(null, "jck-msg");
        check("jck-msg".equals(y.getMessage()), "null target + message ctor: message preserved");
        check(y.getTargetException() == null, "null target + message ctor: target still null");
        check(y.getCause() == null, "null target + message ctor: cause null");
    }

    static void testDeepHierarchy() {
        check(MBeanException.class.getSuperclass() == javax.management.JMException.class, "direct superclass JMException");
        check(new MBeanException(new Exception()) instanceof javax.management.JMException, "extends JMException");
        check(new MBeanException(new IllegalArgumentException("inner-msg")) instanceof java.io.Serializable, "Serializable");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(MBeanException.class).getSerialVersionUID() == 4066342430588744142L,
                "serialVersionUID constant");
        MBeanException src = new MBeanException(new IllegalArgumentException("inner-msg"), "outer-msg");
        MBeanException back = deepRoundTrip(src);
        check("outer-msg".equals(back.getMessage()), "round-trip: message preserved");
        check(back.getTargetException() instanceof IllegalArgumentException, "round-trip: target type");
        check("inner-msg".equals(back.getTargetException().getMessage()), "round-trip: target message preserved");
        check(back.getCause() == back.getTargetException(), "round-trip: getCause() == getTargetException()");
    }

    public static interface ProbeMBean { void boom() throws Exception; }
    public static class Probe implements ProbeMBean {
        public void boom() throws Exception { throw new java.io.IOException("user-io"); }
    }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.mbeanexception:type=Probe");
        server.registerMBean(new Probe(), name);
        try {
            server.invoke(name, "boom", new Object[0], new String[0]);
            check(false, "MBean method throwing Exception must surface as MBeanException");
        } catch (MBeanException e) {
            check(e.getTargetException() instanceof java.io.IOException, "live trigger: wraps user Exception");
            check("user-io".equals(e.getTargetException().getMessage()), "live trigger: user message");
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
