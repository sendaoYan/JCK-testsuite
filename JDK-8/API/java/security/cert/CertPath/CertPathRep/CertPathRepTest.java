/*
 * @test
 * @summary java.security.cert.CertPath.CertPathRep conformance: SerializationRep class hierarchy.
 * @run main CertPathRepTest
 */

public class CertPathRepTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.CertPath$CertPathRep");
        check(!c.isInterface(), "is class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
