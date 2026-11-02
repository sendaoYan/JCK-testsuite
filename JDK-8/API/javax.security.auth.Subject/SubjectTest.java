/*
 * @test
 * @summary javax.security.auth.Subject conformance: constructors, principals, read-only.
 * @run main SubjectTest
 */

public class SubjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.Subject s = new javax.security.auth.Subject();
        check(s.getPrincipals().isEmpty(), "principals empty");
        java.security.Principal p = new javax.security.auth.x500.X500Principal("CN=Test");
        s.getPrincipals().add(p);
        check(s.getPrincipals().contains(p), "add principal");
        check(!s.isReadOnly(), "not read-only");
        s.setReadOnly();
        check(s.isReadOnly(), "setReadOnly");
        javax.security.auth.Subject s2 = new javax.security.auth.Subject();
        check(s2.getPublicCredentials().isEmpty() && s2.getPrivateCredentials().isEmpty(), "empty credentials");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
