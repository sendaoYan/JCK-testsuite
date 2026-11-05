/*
 * @test
 * @summary javax.management.remote.JMXProviderException conformance: exception hierarchy and construction.
 * @run main JMXProviderExceptionTest
 */
import javax.management.*;
import javax.management.remote.*;


public class JMXProviderExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.remote.JMXProviderException x = new javax.management.remote.JMXProviderException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.io.IOException").isAssignableFrom(c), "extends java.io.IOException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): ctor matrix, cause identity, serialization ====
    static void testDeepened() throws Exception {
        JMXProviderException e0 = new JMXProviderException();
        check(e0.getMessage() == null, "d-no-arg message null");
        check(e0.getCause() == null, "d-no-arg cause null");
        JMXProviderException e1 = new JMXProviderException("m");
        check("m".equals(e1.getMessage()), "d-message ctor");
        check(e1.getCause() == null, "d-message ctor cause null");
        java.lang.Throwable cause = new java.lang.AssertionError("inner");
        JMXProviderException e2 = new JMXProviderException("m2", cause);
        check("m2".equals(e2.getMessage()), "d-full ctor message");
        check(e2.getCause() == cause, "d-full ctor cause identity");
        check(e2 instanceof java.io.IOException, "d-IOException hierarchy");
        check(e2 instanceof java.io.Serializable, "d-Serializable");
        check(e2.toString().startsWith("javax.management.remote.JMXProviderException"), "d-toString prefix");
        // Serialization round-trip preserves message and cause.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(e2);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            JMXProviderException back = (JMXProviderException) ois.readObject();
            check("m2".equals(back.getMessage()), "d-ser message");
            check(back.getCause() instanceof java.lang.AssertionError, "d-ser cause type");
            check("inner".equals(back.getCause().getMessage()), "d-ser cause message");
        } finally {
            if (ois != null) ois.close();
        }
    }

}
