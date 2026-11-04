/*
 * @test
 * @summary javax.management.RuntimeErrorException conformance: full contract
 *          coverage. target Error wrapping (getTargetError/getCause), both
 *          constructors, and JMRuntimeException hierarchy.
 * @run main RuntimeErrorExceptionTest
 */

import javax.management.RuntimeErrorException;
import javax.management.*;
import java.io.*;

public class RuntimeErrorExceptionTest {
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
        RuntimeErrorException x = new RuntimeErrorException(null);
        check(x.getTargetError() == null, "null target: getTargetError null");
        check(x.getCause() == null, "null target: getCause null");
        check(x.getMessage() == null, "null target: message null");
        RuntimeErrorException y = new RuntimeErrorException(null, "jck-msg");
        check("jck-msg".equals(y.getMessage()), "null target + message ctor: message preserved");
        check(y.getTargetError() == null, "null target + message ctor: target still null");
        check(y.getCause() == null, "null target + message ctor: cause null");
    }

    static void testDeepHierarchy() {
        check(RuntimeErrorException.class.getSuperclass() == javax.management.JMRuntimeException.class, "direct superclass JMRuntimeException");
        check(new RuntimeErrorException(new AssertionError()).getTargetError() instanceof Error, "getTargetError is an Error");
        check(new RuntimeErrorException(new AssertionError("inner-msg")) instanceof java.io.Serializable, "Serializable");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(RuntimeErrorException.class).getSerialVersionUID() == 704338937753949796L,
                "serialVersionUID constant");
        RuntimeErrorException src = new RuntimeErrorException(new AssertionError("inner-msg"), "outer-msg");
        RuntimeErrorException back = deepRoundTrip(src);
        check("outer-msg".equals(back.getMessage()), "round-trip: message preserved");
        check(back.getTargetError() instanceof AssertionError, "round-trip: target type");
        check("inner-msg".equals(back.getTargetError().getMessage()), "round-trip: target message preserved");
        check(back.getCause() == back.getTargetError(), "round-trip: getCause() == getTargetError()");
    }

    public static interface ProbeMBean { void boomErr(); }
    public static class Probe implements ProbeMBean {
        public void boomErr() { throw new AssertionError("user-error"); }
    }

    // ---- deepened (2026-11): live MBeanServer trigger ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.runtimeerrorexception:type=Probe");
        server.registerMBean(new Probe(), name);
        try {
            server.invoke(name, "boomErr", new Object[0], new String[0]);
            check(false, "MBean method throwing Error must surface as RuntimeErrorException");
        } catch (RuntimeErrorException e) {
            check(e.getTargetError() instanceof AssertionError, "live trigger: getTargetError is the user Error");
            check("user-error".equals(e.getTargetError().getMessage()), "live trigger: error message");
            check(e.getCause() == e.getTargetError(), "live trigger: cause == targetError");
        }
    }

    public static void main(String[] args) throws Exception {
        Error target = new OutOfMemoryError("oom");
        RuntimeErrorException x = new RuntimeErrorException(target);
        check(x.getTargetError() == target, "getTargetError");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMRuntimeException, "extends JMRuntimeException");
        check(x instanceof RuntimeException, "extends RuntimeException");

        RuntimeErrorException y = new RuntimeErrorException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetError() == target, "message ctor target");
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
