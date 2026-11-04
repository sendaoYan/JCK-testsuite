/*
 * @test
 * @summary javax.management.MBeanRegistrationException conformance: white-box structure + black-box behavior.
 * @run main MBeanRegistrationExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;
import java.io.*;

public class MBeanRegistrationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanRegistrationException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("javax.management.MBeanException".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanException");
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
        MBeanRegistrationException e = new MBeanRegistrationException(cause);
        check(e.getMessage() == null, "one-arg message null");
        check(e.getTargetException() == cause, "getTargetException round-trip");
        check(e.getCause() == cause, "getCause mirrors target");
        check(e.toString().contains("MBeanRegistrationException"), "toString contains class name");

        MBeanRegistrationException e2 = new MBeanRegistrationException(cause, "wrapped-message");
        check("wrapped-message".equals(e2.getMessage()), "two-arg message preserved");
        check(e2.getTargetException() == cause, "two-arg getTargetException round-trip");
        check(e2.getCause() == cause, "two-arg getCause mirrors target");
        check(e2.toString().contains("MBeanRegistrationException"), "two-arg toString class name");

        Exception cause2 = new Exception("second");
        MBeanRegistrationException e3 = new MBeanRegistrationException(cause2);
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
        MBeanRegistrationException x = new MBeanRegistrationException(null);
        check(x.getTargetException() == null, "null target: getTargetException null");
        check(x.getCause() == null, "null target: getCause null");
        check(x.getMessage() == null, "null target: message null");
        MBeanRegistrationException y = new MBeanRegistrationException(null, "jck-msg");
        check("jck-msg".equals(y.getMessage()), "null target + message ctor: message preserved");
        check(y.getTargetException() == null, "null target + message ctor: target still null");
        check(y.getCause() == null, "null target + message ctor: cause null");
    }

    static void testDeepHierarchy() {
        check(MBeanRegistrationException.class.getSuperclass() == javax.management.MBeanException.class, "direct superclass MBeanException");
        MBeanRegistrationException x = new MBeanRegistrationException(new Exception("t"));
        check(x.getTargetException().getMessage().equals("t") && x.getCause() == x.getTargetException(),
                "inherits MBeanException wrapping accessors");
        check(new MBeanRegistrationException(new IllegalArgumentException("inner-msg")) instanceof java.io.Serializable, "Serializable");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(MBeanRegistrationException.class).getSerialVersionUID() == 4482382455277067805L,
                "serialVersionUID constant");
        MBeanRegistrationException src = new MBeanRegistrationException(new IllegalArgumentException("inner-msg"), "outer-msg");
        MBeanRegistrationException back = deepRoundTrip(src);
        check("outer-msg".equals(back.getMessage()), "round-trip: message preserved");
        check(back.getTargetException() instanceof IllegalArgumentException, "round-trip: target type");
        check("inner-msg".equals(back.getTargetException().getMessage()), "round-trip: target message preserved");
        check(back.getCause() == back.getTargetException(), "round-trip: getCause() == getTargetException()");
    }

    public static interface ProbeMBean { int getN(); }
    public static class Probe implements ProbeMBean, MBeanRegistration {
        public int getN() { return 1; }
        public ObjectName preRegister(MBeanServer s, ObjectName n) { return n; }
        public void postRegister(Boolean done) { }
        public void preDeregister() throws Exception { throw new java.io.IOException("predereg-fail"); }
        public void postDeregister() { }
    }

    // ---- deepened (2026-11): live MBeanServer trigger (preDeregister failure) ----

    static void testDeepLiveTrigger() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.mbeanregistration:type=Fail");
        server.registerMBean(new Probe(), name);
        try {
            server.unregisterMBean(name);
            check(false, "preDeregister throwing must surface as MBeanRegistrationException");
        } catch (MBeanRegistrationException e) {
            check(e.getTargetException() instanceof java.io.IOException, "wraps user exception from preDeregister");
            check("predereg-fail".equals(e.getTargetException().getMessage()), "wrapped message");
            check(e.getCause() == e.getTargetException(), "cause == targetException");
        }
        check(server.isRegistered(name), "state invariant: bean stays registered after failed deregister");
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
