/*
 * @test
 * @summary javax.management.MBeanException conformance: full contract coverage.
 *          target-exception wrapping (getTargetException/getCause), both
 *          constructors (message defaulting), and JMException hierarchy.
 * @run main MBeanExceptionTest
 */

import javax.management.MBeanException;
import javax.management.*;
import java.io.*;

public class MBeanExceptionTest {
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
        Exception target = new IllegalArgumentException("boom");
        MBeanException x = new MBeanException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMException, "extends JMException");
        check(x instanceof Exception, "extends Exception");

        MBeanException y = new MBeanException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");

        MBeanException noArg = new MBeanException(new Exception());
        check(noArg.getMessage() == null && noArg.getCause() != null, "no-arg + target only");
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
