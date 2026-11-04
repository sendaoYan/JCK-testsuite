/*
 * @test
 * @summary javax.management.MBeanRegistrationException conformance: full
 *          contract coverage. target Exception wrapping (getTargetException/
 *          getCause), both constructors, and MBeanException hierarchy.
 * @run main MBeanRegistrationExceptionTest
 */

import javax.management.MBeanRegistrationException;
import javax.management.*;
import java.io.*;

public class MBeanRegistrationExceptionTest {
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
        Exception target = new IllegalStateException("boom");
        MBeanRegistrationException x = new MBeanRegistrationException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.MBeanException, "extends MBeanException");
        check(x instanceof javax.management.JMException, "extends JMException");

        MBeanRegistrationException y = new MBeanRegistrationException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
