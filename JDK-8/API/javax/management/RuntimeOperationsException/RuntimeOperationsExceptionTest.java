/*
 * @test
 * @summary javax.management.RuntimeOperationsException conformance: full
 *          contract coverage. target RuntimeException wrapping
 *          (getTargetException/getCause), both constructors, and JMRuntimeException
 *          hierarchy.
 * @run main RuntimeOperationsExceptionTest
 */

import javax.management.RuntimeOperationsException;
import javax.management.*;
import java.io.*;

public class RuntimeOperationsExceptionTest {
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
        // JDK-8: registerMBean(null, name) fails with a plain NullPointerException
        // (JDK-27 wraps it in RuntimeOperationsException - locked separately on that side)
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("jck.runtimeops8:type=Null");
        try {
            server.registerMBean(null, name);
            check(false, "registerMBean(null, ...) must throw");
        } catch (NullPointerException e) {
            passed++;
        }
        check(!server.isRegistered(name), "nothing registered after null rejection");
    }

    public static void main(String[] args) throws Exception {
        RuntimeException target = new IndexOutOfBoundsException("oops");
        RuntimeOperationsException x = new RuntimeOperationsException(target);
        check(x.getTargetException() == target, "getTargetException");
        check(x.getCause() == target, "getCause is target");
        check(x.getMessage() == null, "no-arg message null");
        check(x instanceof javax.management.JMRuntimeException, "extends JMRuntimeException");
        check(x instanceof RuntimeException, "extends RuntimeException");

        RuntimeOperationsException y = new RuntimeOperationsException(target, "detail");
        check("detail".equals(y.getMessage()), "message ctor");
        check(y.getTargetException() == target, "message ctor target");
        testDeepNullTargetSemantics();
        testDeepHierarchy();
        testDeepSerialization();
        testDeepLiveTrigger();
        testDeepVersionSpecificTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
