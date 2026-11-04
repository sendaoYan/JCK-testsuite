/*
 * @test
 * @summary javax.management.RuntimeMBeanException conformance: full contract
 *          coverage. target RuntimeException wrapping (getTargetException/
 *          getCause), both constructors, and JMRuntimeException hierarchy.
 * @run main RuntimeMBeanExceptionTest
 */

import javax.management.RuntimeMBeanException;
import javax.management.*;
import java.io.*;

public class RuntimeMBeanExceptionTest {
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
        RuntimeException target = new IllegalStateException("boom");
        RuntimeMBeanException x = new RuntimeMBeanException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMRuntimeException, "extends JMRuntimeException");
        check(x instanceof RuntimeException, "extends RuntimeException");

        RuntimeMBeanException y = new RuntimeMBeanException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
