/*
 * @test
 * @summary javax.management.RuntimeErrorException conformance: white-box structure + black-box behavior.
 * @run main RuntimeErrorExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class RuntimeErrorExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.RuntimeErrorException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.JMRuntimeException".equals(c.getSuperclass().getName()), "superclass javax.management.JMRuntimeException");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        java.lang.reflect.Constructor<?> ctor;
        ctor = c.getConstructor(Error.class);
        check(ctor != null, "Error constructor");
        ctor = c.getConstructor(Error.class, String.class);
        check(ctor != null, "Error + String constructor");
        c.getMethod("getTargetError");
        c.getMethod("getCause");
    }
    static void testBlackBox() throws Exception {
        Error cause = new Error("cause-message");
        RuntimeErrorException e = new RuntimeErrorException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetError() == cause, "getTargetError round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("RuntimeErrorException"), "toString contains class name");

        RuntimeErrorException e2 = new RuntimeErrorException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetError() == cause, "two-arg getTargetError round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("RuntimeErrorException"), "two-arg toString class name");

        Error cause2 = new Error("second");
        RuntimeErrorException e3 = new RuntimeErrorException(cause2);
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
        testWhiteBox();
        testBlackBox();
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
