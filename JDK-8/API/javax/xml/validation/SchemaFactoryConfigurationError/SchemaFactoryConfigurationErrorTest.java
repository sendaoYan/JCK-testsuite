/*
 * @test
 * @summary javax.xml.validation.SchemaFactoryConfigurationError conformance: exception hierarchy and constructors.
 * @run main SchemaFactoryConfigurationErrorTest
 */


public class SchemaFactoryConfigurationErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.validation.SchemaFactoryConfigurationError.class.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static void testConstructors() throws Exception {
        // () no message, no cause
        javax.xml.validation.SchemaFactoryConfigurationError e0 =
            new javax.xml.validation.SchemaFactoryConfigurationError();
        check(e0.getMessage() == null, "() message null");
        check(e0.getCause() == null, "() cause null");

        // (String)
        javax.xml.validation.SchemaFactoryConfigurationError e1 =
            new javax.xml.validation.SchemaFactoryConfigurationError("cfg broken");
        check("cfg broken".equals(e1.getMessage()), "(String) message round-trip");
        check(e1.getCause() == null, "(String) cause null");

        // (Throwable)
        RuntimeException cause = new RuntimeException("root cause");
        javax.xml.validation.SchemaFactoryConfigurationError e2 =
            new javax.xml.validation.SchemaFactoryConfigurationError(cause);
        check(e2.getCause() == cause, "(Throwable) cause round-trip");
        check(e2.getMessage() != null && e2.getMessage().contains("root cause"),
              "(Throwable) message derived from cause");

        // (String, Throwable)
        javax.xml.validation.SchemaFactoryConfigurationError e3 =
            new javax.xml.validation.SchemaFactoryConfigurationError("msg", cause);
        check("msg".equals(e3.getMessage()), "(String,Throwable) message round-trip");
        check(e3.getCause() == cause, "(String,Throwable) cause round-trip");

        // final class, unchecked Error semantics
        check(java.lang.reflect.Modifier.isFinal(
                  javax.xml.validation.SchemaFactoryConfigurationError.class.getModifiers()),
              "class is final");
        check(e3 instanceof Error, "instanceof Error (unchecked)");
        check(e3.toString().contains("msg"), "toString carries message");

        // serialization round-trip (Error is Serializable)
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(e3);
            oos.close();
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
            javax.xml.validation.SchemaFactoryConfigurationError back =
                (javax.xml.validation.SchemaFactoryConfigurationError) ois.readObject();
            check("msg".equals(back.getMessage()), "serialization round-trip message");
            check(back.getCause() instanceof RuntimeException
                  && "root cause".equals(back.getCause().getMessage()), "serialization round-trip cause");
        } catch (java.io.IOException e) {
            check(false, "serialization round-trip unexpected IOException: " + e);
        }
    }
}
