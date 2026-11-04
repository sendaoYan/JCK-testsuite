/*
 * @test
 * @summary javax.security.auth.x500.X500PrivateCredential conformance: class hierarchy and methods.
 * @run main X500PrivateCredentialTest
 */

public class X500PrivateCredentialTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.auth.x500.X500PrivateCredential");
        check(!c.isInterface(), "is class");
        check(javax.security.auth.Destroyable.class.isAssignableFrom(c), "implements Destroyable");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPrivateKey"), "declares getPrivateKey");
        check(ms.contains("getCertificate"), "declares getCertificate");
        check(ms.contains("destroy"), "declares destroy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
