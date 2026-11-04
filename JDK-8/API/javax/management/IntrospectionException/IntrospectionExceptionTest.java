/*
 * @test
 * @summary javax.management.IntrospectionException conformance: exception hierarchy and construction.
 * @run main IntrospectionExceptionTest
 */
import javax.management.*;
import java.io.*;

public class IntrospectionExceptionTest {
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

    // ---- deepened (2026-11): ctor chain / hierarchy / serialVersionUID / serialization ----

    static void testDeepCtorChain() {
        IntrospectionException noArg = new IntrospectionException();
        check(noArg.getMessage() == null, "no-arg ctor: message null");
        check(noArg.getCause() == null, "no-arg ctor: cause null");
        IntrospectionException msg = new IntrospectionException("jck-detail");
        check("jck-detail".equals(msg.getMessage()), "String ctor: message preserved");
        check("jck-detail".equals(msg.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        IntrospectionException nullMsg = new IntrospectionException((String) null);
        check(nullMsg.getMessage() == null, "null String message -> null message");
        check(IntrospectionException.class.getSuperclass() == javax.management.OperationsException.class, "direct superclass OperationsException");
        check(msg instanceof javax.management.OperationsException
                && msg instanceof javax.management.JMException
                && msg instanceof Exception
                && msg instanceof java.io.Serializable, "hierarchy: OperationsException/JMException/Exception/Serializable");
        check(!RuntimeException.class.isAssignableFrom(msg.getClass()), "checked exception, not runtime");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(IntrospectionException.class).getSerialVersionUID() == 1054516935875481725L,
                "serialVersionUID constant (" + 1054516935875481725L + ")");
        IntrospectionException backNo = deepRoundTrip(new IntrospectionException());
        check(backNo instanceof IntrospectionException && backNo.getMessage() == null, "round-trip no-arg: class + null message");
        IntrospectionException backMsg = deepRoundTrip(new IntrospectionException("jck-ser-msg"));
        check("jck-ser-msg".equals(backMsg.getMessage()), "round-trip: message preserved");
        check(backMsg.getCause() == null, "round-trip: no cause");
        check(msgToStringKeepsClass(backMsg), "round-trip: toString keeps class name");
    }

    static boolean msgToStringKeepsClass(Throwable t) {
        return t.toString().contains("IntrospectionException");
    }

    public static void main(String[] args) throws Exception {
        javax.management.IntrospectionException x = new javax.management.IntrospectionException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.OperationsException").isAssignableFrom(c), "extends javax.management.OperationsException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepCtorChain();
        testDeepSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
