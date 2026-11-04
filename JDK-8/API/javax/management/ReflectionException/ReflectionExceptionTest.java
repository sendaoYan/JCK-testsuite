/*
 * @test
 * @summary javax.management.ReflectionException conformance: full contract
 *          coverage. target Exception wrapping (getTargetException/getCause),
 *          both constructors, and JMException hierarchy.
 * @run main ReflectionExceptionTest
 */

import javax.management.ReflectionException;
import javax.management.*;
import java.io.*;

public class ReflectionExceptionTest {
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
        Exception target = new NoSuchMethodException("nope");
        ReflectionException x = new ReflectionException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMException, "extends JMException");

        ReflectionException y = new ReflectionException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
